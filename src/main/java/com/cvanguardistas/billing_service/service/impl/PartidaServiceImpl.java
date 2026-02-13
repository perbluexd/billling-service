// src/main/java/com/cvanguardistas/billing_service/service/impl/PartidaServiceImpl.java
package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.dto.*;
import com.cvanguardistas.billing_service.entities.*;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.*;
import com.cvanguardistas.billing_service.service.*;
import com.cvanguardistas.billing_service.service.mapper.PartidaMapper;
import com.cvanguardistas.billing_service.web.audit.Auditable;
import com.cvanguardistas.billing_service.entities.AccionAuditoria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartidaServiceImpl implements PartidaService {

    private final PartidaRepository partidaRepo;
    private final PartidaInsumoRepository partidaInsumoRepo;
    private final PartidaTotalCategoriaRepository ptcRepo;
    private final CategoriaCostoRepository categoriaRepo;
    private final SubPresupuestoRepository SubPresRepo;
    private final UnidadRepository unidadRepo;
    private final InsumoRepository insumoRepo;
    private final PlantillaPartidaRepository plantillaPartidaRepo;
    // <— agregado para instanciar desde catálogo

    private final PrecioService precioService;
    private final CalculoACUService calculoACUService;
    private final PartidaMapper partidaMapper;
    private final GGItemService ggItemService;
    private final SPFormulaService spFormulaService;

    @Override
    @Transactional
    @Auditable(
            entidad = "Partida",
            accion = AccionAuditoria.EDITAR,
            entityId = "#cmd.partidaId"
    )
    public HojaDto actualizarHoja(HojaUpdateCmd cmd) {
        // 1) Cargar hoja y validar tipo
        Partida hoja = partidaRepo.findById(cmd.partidaId())
                .orElseThrow(() -> new DomainException("Partida no encontrada: " + cmd.partidaId()));
        if (hoja.getTipo() != TipoPartida.HOJA) {
            throw new DomainException("Solo se pueden actualizar partidas de tipo HOJA");
        }

        // 2) Aplicar cambios básicos
        if (cmd.unidadId() != null) {
            Unidad unidad = unidadRepo.getReferenceById(cmd.unidadId());
            hoja.setUnidad(unidad);
        }
        if (cmd.rendimiento() != null) hoja.setRendimiento(cmd.rendimiento());
        if (cmd.metrado() != null) hoja.setMetrado(cmd.metrado());
        partidaRepo.save(hoja);

        // 3) Materializar/actualizar líneas si vinieron en el comando
        List<PartidaInsumo> lineasActuales = partidaInsumoRepo.findByPartidaId(hoja.getId());
        if (cmd.lineas() != null && !cmd.lineas().isEmpty()) {
            for (PartidaInsumo e : lineasActuales) partidaInsumoRepo.delete(e);
            lineasActuales = new ArrayList<>(cmd.lineas().size());
            for (LineaACURequest req : cmd.lineas()) {
                PartidaInsumo e = new PartidaInsumo();
                e.setPartida(hoja);
                e.setInsumo(insumoRepo.getReferenceById(req.insumoId()));
                e.setCategoriaCosto(categoriaRepo.getReferenceById(req.categoriaCostoId()));
                e.setDependeDeRendimiento(Boolean.TRUE.equals(req.dependeDeRendimiento()));
                e.setCuadrillaFrac(req.cuadrillaFrac());
                e.setCantidadFija(req.cantidadFija());
                e.setPuOverride(req.puOverride());
                e.setUsarPuOverride(Boolean.TRUE.equals(req.usarPuOverride()));
                lineasActuales.add(partidaInsumoRepo.save(e));
            }
        }

        // 4) Preparar entrada para motor de cálculo (resolver PU efectivo)
        SubPresupuesto sp = hoja.getSubPresupuesto();
        if (sp == null) throw new DomainException("Hoja sin SubPresupuesto");
        Presupuesto presupuesto = sp.getPresupuesto();
        if (presupuesto == null) throw new DomainException("SubPresupuesto sin presupuesto");

        BigDecimal jornadaEfectiva = hoja.getJornadaHorasOverride() != null
                ? hoja.getJornadaHorasOverride()
                : (presupuesto.getJornadaHoras() != null ? presupuesto.getJornadaHoras() : BigDecimal.valueOf(8));

        LocalDate fechaBase = presupuesto.getFechaBase();

        List<LineaACURequest> lineasParaCalculo = lineasActuales.stream().map(e -> {
            BigDecimal puEfectivo;
            if (Boolean.TRUE.equals(e.getUsarPuOverride()) && e.getPuOverride() != null) {
                puEfectivo = e.getPuOverride();
            } else {
                puEfectivo = precioService.precioVigente(e.getInsumo().getId(), fechaBase);
            }
            return new LineaACURequest(
                    e.getInsumo().getId(),
                    e.getCategoriaCosto().getId(),
                    Boolean.TRUE.equals(e.getDependeDeRendimiento()),
                    e.getCuadrillaFrac(),
                    e.getCantidadFija(),
                    puEfectivo,
                    true
            );
        }).toList();

        HojaCalcRequest in = new HojaCalcRequest(
                jornadaEfectiva,
                hoja.getRendimiento(),
                hoja.getMetrado(),
                lineasParaCalculo
        );

        // 5) Calcular
        HojaDto out = calculoACUService.calcular(in);

        // 6) Sincronizar cantidades/PU/parcial en PartidaInsumo
        if (out.lineas().size() != lineasActuales.size()) {
            throw new DomainException("Inconsistencia: líneas calculadas != líneas persistidas");
        }
        for (int i = 0; i < lineasActuales.size(); i++) {
            PartidaInsumo e = lineasActuales.get(i);
            LineaACUDto l = out.lineas().get(i);
            e.setCantidad(l.cantidad());
            e.setPu(l.pu());
            e.setParcial(l.parcial());
            partidaInsumoRepo.save(e);
        }

        // 7) Materializar chips (PTC)
        for (ChipDto c : out.chips()) {
            PartidaTotalCategoriaId id = new PartidaTotalCategoriaId(hoja.getId(), c.categoriaCostoId());
            PartidaTotalCategoria row = ptcRepo.findById(id).orElse(new PartidaTotalCategoria());
            row.setId(id);
            row.setPartida(hoja);
            row.setCategoriaCosto(categoriaRepo.getReferenceById(c.categoriaCostoId()));
            row.setUnitarioCalc(c.unitarioCalc());
            row.setTotalCalc(c.totalCalc());
            ptcRepo.save(row);
        }

        // 7.1) LIMPIAR chips huérfanos
        Set<Long> catsNuevas = out.chips().stream()
                .map(ChipDto::categoriaCostoId)
                .collect(Collectors.toSet());
        if (catsNuevas.isEmpty()) {
            ptcRepo.deleteByPartidaId(hoja.getId());
        } else {
            ptcRepo.deleteByPartidaIdAndCategoriaCostoIdNotIn(hoja.getId(), catsNuevas);
        }

        // 8) Calcular CU final
        Set<Long> categoriasIncluidas = categoriaRepo.findAll().stream()
                .filter(cc -> Boolean.TRUE.equals(cc.getIncluyeEnCu()))
                .map(CategoriaCosto::getId)
                .collect(Collectors.toSet());

        List<PartidaTotalCategoria> chips = ptcRepo.findByPartidaId(hoja.getId());
        BigDecimal cu = BigDecimal.ZERO;
        for (PartidaTotalCategoria row : chips) {
            Long catId = row.getCategoriaCosto().getId();
            if (!categoriasIncluidas.contains(catId)) continue;
            BigDecimal unit = Boolean.TRUE.equals(row.getUsarOverride()) && row.getUnitarioOverride() != null
                    ? row.getUnitarioOverride()
                    : row.getUnitarioCalc();
            if (unit != null) cu = cu.add(unit);
        }
        cu = cu.setScale(2, RoundingMode.HALF_UP);

        hoja.setCu(cu);
        hoja.setParcial(
                cu.multiply(hoja.getMetrado() == null ? BigDecimal.ZERO : hoja.getMetrado())
                        .setScale(2, RoundingMode.HALF_UP)
        );
        partidaRepo.save(hoja);

        // 9) Propagar hacia padres
        propagarHaciaArriba(hoja);

        // 10) Recalcular cd_total del SP
        recalcularCdTotal(sp.getId());

        // 10.1) GG y Pie
        BigDecimal cd = SubPresRepo.findById(sp.getId())
                .map(SubPresupuesto::getCdTotal)
                .orElse(BigDecimal.ZERO);

        BigDecimal gg = ggItemService.recalcularGG(sp.getId(), cd);

        Map<String, BigDecimal> base = new HashMap<>();
        base.put("CD", cd);
        base.put("GG", gg);

        spFormulaService.evaluarPie(sp.getId(), base);

        // 11) DTO consistente
        List<PartidaInsumo> lineasRef = partidaInsumoRepo.findByPartidaId(hoja.getId());
        List<PartidaTotalCategoria> chipsRef = ptcRepo.findByPartidaId(hoja.getId());
        return partidaMapper.toHojaDto(hoja, lineasRef, chipsRef);
    }

    @Override
    @Transactional(readOnly = true)
    public HojaDto obtenerHoja(Long partidaId) {
        Partida hoja = partidaRepo.findById(partidaId)
                .orElseThrow(() -> new DomainException("Partida no encontrada: " + partidaId));
        if (hoja.getTipo() != TipoPartida.HOJA) {
            throw new DomainException("Solo aplica a hojas");
        }
        var lineas = partidaInsumoRepo.findByPartidaId(hoja.getId());
        var chips = ptcRepo.findByPartidaId(hoja.getId());
        return partidaMapper.toHojaDto(hoja, lineas, chips);
    }

    // ===========================
    // MOVER PARTIDA (reforzado)
    // ===========================
    @Override
    @Transactional
    @Auditable(entidad = "Partida", accion = AccionAuditoria.EDITAR, entityId = "#partidaId")
    public void moverPartida(Long partidaId, Long nuevoPadreId, Integer nuevoOrden) {
        Partida p = partidaRepo.findById(partidaId)
                .orElseThrow(() -> new DomainException("Partida no encontrada: " + partidaId));

        Partida padreActual = p.getPadre();
        Long spId = p.getSubPresupuesto().getId();

        Partida nuevoPadre = null;
        if (nuevoPadreId != null) {
            nuevoPadre = partidaRepo.findById(nuevoPadreId)
                    .orElseThrow(() -> new DomainException("Padre destino no encontrado: " + nuevoPadreId));

            // Regla: no permitir padre HOJA
            if (nuevoPadre.getTipo() == TipoPartida.HOJA) {
                throw new DomainException("No puedes mover una partida debajo de una HOJA");
            }

            // Misma obra (mismo SubPresupuesto)
            if (!Objects.equals(nuevoPadre.getSubPresupuesto().getId(), spId)) {
                throw new DomainException("No puedes mover la partida a otro SubPresupuesto");
            }
        }

        // Si no viene orden, lo ponemos al final
        if (nuevoOrden == null || nuevoOrden < 1) {
            int tam = (nuevoPadre == null)
                    ? partidaRepo.findBySubPresupuestoIdAndPadreIsNullOrderByOrdenAsc(spId).size()
                    : partidaRepo.findByPadreOrderByOrdenAsc(nuevoPadre).size();
            nuevoOrden = tam + 1;
        }

        // 1) Saco a 'p' de su grupo actual y compacto orden
        if (padreActual == null) {
            List<Partida> raices = partidaRepo.findBySubPresupuestoIdAndPadreIsNullOrderByOrdenAsc(spId);
            raices.removeIf(x -> x.getId().equals(p.getId()));
            int idx = 1; for (Partida r : raices) { r.setOrden(idx++); partidaRepo.save(r); }
        } else {
            List<Partida> hermanos = partidaRepo.findByPadreOrderByOrdenAsc(padreActual);
            hermanos.removeIf(x -> x.getId().equals(p.getId()));
            int idx = 1; for (Partida h : hermanos) { h.setOrden(idx++); partidaRepo.save(h); }
        }

        // 2) Inserto en el nuevo grupo y desplazo a partir de 'nuevoOrden'
        if (nuevoPadre == null) {
            List<Partida> raices = partidaRepo.findBySubPresupuestoIdAndPadreIsNullOrderByOrdenAsc(spId);
            for (Partida r : raices) {
                if (r.getOrden() != null && r.getOrden() >= nuevoOrden) {
                    r.setOrden(r.getOrden() + 1);
                    partidaRepo.save(r);
                }
            }
            p.setPadre(null);
            p.setOrden(nuevoOrden);
        } else {
            List<Partida> hermanos = partidaRepo.findByPadreOrderByOrdenAsc(nuevoPadre);
            for (Partida h : hermanos) {
                if (h.getOrden() != null && h.getOrden() >= nuevoOrden) {
                    h.setOrden(h.getOrden() + 1);
                    partidaRepo.save(h);
                }
            }
            p.setPadre(nuevoPadre);
            p.setOrden(nuevoOrden);
        }
        partidaRepo.save(p);

        // 3) Propagar totales: desde el padre actual y desde el nuevo padre
        if (padreActual != null) propagarHaciaArriba(padreActual);
        if (nuevoPadre != null) propagarHaciaArriba(nuevoPadre);

        // 4) Recalcular CD, GG, PIE (en caso cambie el agrupamiento)
        recalcularCdTotal(spId);

        BigDecimal cd = SubPresRepo.findById(spId)
                .map(SubPresupuesto::getCdTotal)
                .orElse(BigDecimal.ZERO);
        BigDecimal gg = ggItemService.recalcularGG(spId, cd);

        Map<String, BigDecimal> base = new HashMap<>();
        base.put("CD", cd);
        base.put("GG", gg);
        spFormulaService.evaluarPie(spId, base);
    }

    // ===== helpers =====

    private void propagarHaciaArriba(Partida hoja) {
        Partida actual = hoja.getPadre();
        while (actual != null) {
            List<Partida> hijos = partidaRepo.findByPadreOrderByOrdenAsc(actual);
            BigDecimal suma = BigDecimal.ZERO;
            for (Partida h : hijos) {
                if (h.getParcial() != null) suma = suma.add(h.getParcial());
            }
            actual.setParcial(suma);
            actual.setCu(null); // títulos/Subtítulos no tienen CU
            partidaRepo.save(actual);
            actual = actual.getPadre();
        }
    }

    private void recalcularCdTotal(Long spId) {
        List<Partida> arbol = partidaRepo.findArbolBySubPresupuesto(spId);
        BigDecimal cd = BigDecimal.ZERO;
        for (Partida p : arbol) {
            if (p.getTipo() == TipoPartida.HOJA && p.getParcial() != null) {
                cd = cd.add(p.getParcial());
            }
        }
        SubPresupuesto sp = SubPresRepo.findById(spId)
                .orElseThrow(() -> new DomainException("SP no encontrado: " + spId));
        sp.setCdTotal(cd);
        SubPresRepo.save(sp);
    }

    @Override
    @Transactional
    @Auditable(entidad = "Partida", accion = AccionAuditoria.CREAR, entityIdFromResult = "#result")
    public Long crear(Long subPresupuestoId,
                      Long padreId,
                      TipoPartida tipo,
                      String codigo,
                      String nombre,
                      Long unidadId,
                      BigDecimal rendimiento,
                      BigDecimal metrado,
                      Integer orden) {

        SubPresupuesto sp = SubPresRepo.findById(subPresupuestoId)
                .orElseThrow(() -> new DomainException("SubPresupuesto no encontrado: " + subPresupuestoId));

        Partida padre = (padreId == null) ? null :
                partidaRepo.findById(padreId).orElseThrow(() -> new DomainException("Padre no encontrado: " + padreId));

        Partida p = new Partida();
        p.setSubPresupuesto(sp);
        p.setPadre(padre);
        p.setTipo(tipo);
        p.setCodigo(codigo);
        p.setNombre(nombre);

        if (tipo == TipoPartida.HOJA) {
            if (unidadId == null || rendimiento == null || metrado == null) {
                throw new DomainException("Para HOJA: unidadId, rendimiento y metrado son obligatorios");
            }
            p.setUnidad(unidadRepo.getReferenceById(unidadId));
            p.setRendimiento(rendimiento);
            p.setMetrado(metrado);
            p.setCu(BigDecimal.ZERO);
            p.setParcial(BigDecimal.ZERO);
        } else {
            p.setUnidad(null);
            p.setRendimiento(null);
            p.setMetrado(null);
            p.setCu(null);
            p.setParcial(BigDecimal.ZERO);
        }

        if (orden == null) {
            int next;
            if (padre == null) {
                next = partidaRepo.findBySubPresupuestoIdAndPadreIsNullOrderByOrdenAsc(subPresupuestoId).size() + 1;
            } else {
                next = partidaRepo.findByPadreOrderByOrdenAsc(padre).size() + 1;
            }
            p.setOrden(next);
        } else {
            p.setOrden(orden);
        }

        p = partidaRepo.save(p);
        return p.getId();
    }

    // =========================
    // NUEVO: instanciarDesdeCatalogo
    // =========================
    @Override
    @Transactional
    @Auditable(entidad = "Partida", accion = AccionAuditoria.CREAR, entityIdFromResult = "#result")
    public Long instanciarDesdeCatalogo(Long subPresupuestoId, Long catalogoPartidaId) {
        var sp = SubPresRepo.findById(subPresupuestoId)
                .orElseThrow(() -> new DomainException("SubPresupuesto no encontrado: " + subPresupuestoId));

        var plantillaPartida = plantillaPartidaRepo.findById(catalogoPartidaId)
                .orElseThrow(() -> new DomainException("Catálogo no encontrado: " + catalogoPartidaId));

        Partida nueva = new Partida();
        nueva.setSubPresupuesto(sp);
        nueva.setTipo(TipoPartida.HOJA);
        nueva.setCodigo(plantillaPartida.getCodigo());
        nueva.setNombre(plantillaPartida.getNombre());
        nueva.setUnidad(plantillaPartida.getUnidad());
        nueva.setRendimiento(plantillaPartida.getRendimientoBase());   // ✅ correcto
        nueva.setMetrado(plantillaPartida.getCantidadBase());          // ✅ correcto
        nueva.setCu(plantillaPartida.getPrecioUnitRef());              // ✅ correcto
        nueva.setParcial(plantillaPartida.getPrecioUnitRef());
        nueva.setOrden(plantillaPartida.getOrden());

        nueva = partidaRepo.save(nueva);
        return nueva.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartidaArbolDto> obtenerArbol(Long subPresupuestoId) {
        // Trae todas las partidas del SP (ya ordenadas por padre/orden si usas tu query)
        List<Partida> flat = partidaRepo.findArbolBySubPresupuesto(subPresupuestoId);

        // Indexar hijos por padre
        Map<Long, List<Partida>> childrenMap = new HashMap<>();
        for (Partida p : flat) {
            Long padreId = (p.getPadre() != null) ? p.getPadre().getId() : null;
            if (padreId != null) {
                childrenMap.computeIfAbsent(padreId, k -> new ArrayList<>()).add(p);
            }
        }

        // Raíces (sin padre)
        return flat.stream()
                .filter(p -> p.getPadre() == null)
                .sorted(Comparator.comparing(Partida::getOrden, Comparator.nullsLast(Integer::compareTo)))
                .map(p -> toDtoTree(p, childrenMap))
                .toList();
    }

    @Override
    @Transactional
    @Auditable(entidad = "Partida", accion = AccionAuditoria.ELIMINAR, entityId = "#partidaId")
    public void eliminar(Long partidaId) {
        Partida p = partidaRepo.findById(partidaId)
                .orElseThrow(() -> new DomainException("Partida no encontrada: " + partidaId));

        // Regla: bloquear si tiene hijos
        List<Partida> hijos = partidaRepo.findByPadreOrderByOrdenAsc(p);
        if (!hijos.isEmpty()) {
            throw new DomainException("No puedes eliminar una partida que tiene subpartidas. " +
                    "Elimina o reubica primero sus hijos.");
        }

        // Si es HOJA: limpia ACU y chips
        if (p.getTipo() == TipoPartida.HOJA) {
            ptcRepo.deleteByPartidaId(p.getId());
            var lineas = partidaInsumoRepo.findByPartidaId(p.getId());
            partidaInsumoRepo.deleteAll(lineas);
        }

        Partida padre = p.getPadre();
        Long spId = p.getSubPresupuesto().getId();

        partidaRepo.delete(p);

        // Reordenar hermanos del padre (compactar 'orden')
        if (padre != null) {
            List<Partida> hermanos = partidaRepo.findByPadreOrderByOrdenAsc(padre);
            int idx = 1;
            for (Partida h : hermanos) {
                if (h.getOrden() == null || h.getOrden() != idx) {
                    h.setOrden(idx);
                    partidaRepo.save(h);
                }
                idx++;
            }
        } else {
            List<Partida> raices = partidaRepo.findBySubPresupuestoIdAndPadreIsNullOrderByOrdenAsc(spId);
            int idx = 1;
            for (Partida r : raices) {
                if (r.getOrden() == null || r.getOrden() != idx) {
                    r.setOrden(idx);
                    partidaRepo.save(r);
                }
                idx++;
            }
        }

        // Propagar totales hacia arriba desde el padre
        if (padre != null) {
            propagarHaciaArriba(padre);
        }

        // Recalcular CD del SP, luego GG y PIE
        recalcularCdTotal(spId);

        BigDecimal cd = SubPresRepo.findById(spId)
                .map(SubPresupuesto::getCdTotal)
                .orElse(BigDecimal.ZERO);
        BigDecimal gg = ggItemService.recalcularGG(spId, cd);

        Map<String, BigDecimal> base = new HashMap<>();
        base.put("CD", cd);
        base.put("GG", gg);
        spFormulaService.evaluarPie(spId, base);
    }

    private PartidaArbolDto toDtoTree(Partida p, Map<Long, List<Partida>> childrenMap) {
        List<Partida> hijosEntities = childrenMap.getOrDefault(p.getId(), List.of());
        List<PartidaArbolDto> hijos = hijosEntities.stream()
                .sorted(Comparator.comparing(Partida::getOrden, Comparator.nullsLast(Integer::compareTo)))
                .map(child -> toDtoTree(child, childrenMap))
                .toList();

        return new PartidaArbolDto(
                p.getId(),
                p.getTipo() != null ? p.getTipo().name() : null,
                p.getNombre(),
                p.getOrden(),
                p.getMo(), p.getMt(), p.getEq(), p.getSc(), p.getSp(),
                p.getCu(), p.getParcial(),
                hijos
        );
    }
}
