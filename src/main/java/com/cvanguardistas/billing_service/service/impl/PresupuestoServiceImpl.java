package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.dto.PagedResponse;
import com.cvanguardistas.billing_service.dto.PresupuestoDetalleDto;
import com.cvanguardistas.billing_service.dto.PresupuestoListItemDto;
import com.cvanguardistas.billing_service.entities.*;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.*;
import com.cvanguardistas.billing_service.service.PresupuestoService;
import com.cvanguardistas.billing_service.service.mapper.PresupuestoMapper;
import com.cvanguardistas.billing_service.service.mapper.SubPresupuestoMapper; // <- PascalCase
import com.cvanguardistas.billing_service.web.audit.Auditable;
import com.cvanguardistas.billing_service.entities.AccionAuditoria;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de Presupuesto con snapshot JSON basado en JsonNode.
 */
@Service
@RequiredArgsConstructor
public class PresupuestoServiceImpl implements PresupuestoService {

    private final PresupuestoRepository presupuestoRepo;
    private final SubPresupuestoRepository SubPresRepo;
    private final PartidaRepository partidaRepo;
    private final PartidaInsumoRepository partidaInsumoRepo;
    private final PartidaTotalCategoriaRepository ptcRepo;
    private final PresupuestoSnapshotRepository snapshotRepo;
    private final GrupoPresupuestoRepository grupoRepo; // 4.1 Inyectar el repo de grupos
    private final ObjectMapper objectMapper;

    // mappers
    private final PresupuestoMapper presupuestoMapper;
    private final SubPresupuestoMapper SubPresupuestoMapper; // <- tipo y nombre alineados

    // ============================
    // Crear en blanco
    // ============================
    @Override
    @Transactional
    @Auditable(
            entidad = "Presupuesto",
            accion = AccionAuditoria.CREAR,
            entityIdFromResult = "#result"
    )
    public Long crearEnBlanco(String nombre, LocalDate fechaBase, String moneda, BigDecimal jornadaHoras) {
        Presupuesto p = new Presupuesto();
        p.setNombre(nombre);
        p.setFechaBase(fechaBase != null ? fechaBase : LocalDate.now());
        p.setMoneda(moneda != null ? moneda : "PEN");
        p.setJornadaHoras(jornadaHoras != null ? jornadaHoras : BigDecimal.valueOf(8));
        p.setEstado(EstadoPresupuesto.BORRADOR);
        p = presupuestoRepo.save(p);
        return p.getId();
    }

    // ============================
    // Aprobar + snapshot JSON
    // ============================
    @Override
    @Transactional
    @Auditable(
            entidad = "Presupuesto",
            accion = AccionAuditoria.APROBAR,
            entityId = "#presupuestoId"
    )
    public void aprobar(Long presupuestoId, String version) {
        Presupuesto p = presupuestoRepo.findById(presupuestoId)
                .orElseThrow(() -> new DomainException("Presupuesto no encontrado: " + presupuestoId));

        if (version != null && snapshotRepo.findByPresupuesto_IdAndVersion(presupuestoId, version).isPresent()) {
            throw new DomainException("Ya existe un snapshot con versión: " + version);
        }

        // 1) Congelar PU de todas las líneas del presupuesto
        List<PartidaInsumo> lineas = partidaInsumoRepo.findByPartida_SubPresupuesto_Presupuesto_Id(presupuestoId);
        for (PartidaInsumo li : lineas) {
            li.setPuCongelado(li.getPu());
        }
        partidaInsumoRepo.saveAll(lineas);

        // 2) Cambiar estado
        p.setEstado(EstadoPresupuesto.APROBADO);
        presupuestoRepo.save(p);

        // 3) Crear snapshot JSON (JsonNode)
        JsonNode jsonNode = buildSnapshotJson(p);

        PresupuestoSnapshot snap = PresupuestoSnapshot.builder()
                .presupuesto(p)
                .version(version != null ? version : UUID.randomUUID().toString())
                .jsonSnapshot(jsonNode)
                .build();

        snapshotRepo.save(snap);
    }

    // ============================
    // Listar (legacy) -> delega
    // ============================
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PresupuestoListItemDto> listar(int page, int size) {
        // Delegamos al nuevo con filtros nulos para mantener compatibilidad
        return listar(page, size, null, null);
    }

    // ============================
    // 4.2 Listado con filtros (nuevo)
    // ============================
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PresupuestoListItemDto> listar(int page, int size, String grupo, String q) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 200));

        // Normaliza parámetros
        String grupoParam = (grupo == null || grupo.isBlank()) ? null : grupo.trim().toLowerCase();
        String qLower     = (q == null || q.isBlank()) ? ""   : q.trim().toLowerCase();

        Page<Presupuesto> pg = presupuestoRepo.searchByGrupoAndTexto(grupoParam, qLower, pageable);

        var items = pg.getContent().stream().map(p -> {
            var sps = SubPresRepo.findByPresupuestoIdOrderByIdAsc(p.getId());
            var totalCd = sps.stream()
                    .map(sp -> sp.getCdTotal() == null ? BigDecimal.ZERO : sp.getCdTotal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return presupuestoMapper.toListItem(p, totalCd);
        }).toList();

        return new PagedResponse<>(items, pg.getNumber(), pg.getSize(), pg.getTotalElements(), pg.getTotalPages());
    }

    // ============================
    // Detalle
    // ============================
    @Override
    @Transactional(readOnly = true)
    public PresupuestoDetalleDto detalle(Long id) {
        Presupuesto p = presupuestoRepo.findById(id)
                .orElseThrow(() -> new DomainException("Presupuesto no encontrado: " + id));

        var sps = SubPresRepo.findByPresupuestoIdOrderByIdAsc(id);

        var Subres = sps.stream()
                .map(SubPresupuestoMapper::toResumen) // usa el mapper inyectado
                .collect(Collectors.toList());

        var totalCd = sps.stream()
                .map(sp -> sp.getCdTotal() == null ? BigDecimal.ZERO : sp.getCdTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return presupuestoMapper.toDetalle(p, totalCd, Subres);
    }

    // ============================
    // Renombrar
    // ============================
    @Override
    @Transactional
    @Auditable(entidad = "Presupuesto", accion = AccionAuditoria.EDITAR, entityId = "#id")
    public void renombrar(Long id, String nuevoNombre) {
        Presupuesto p = presupuestoRepo.findById(id)
                .orElseThrow(() -> new DomainException("Presupuesto no encontrado: " + id));
        p.setNombre(nuevoNombre);
        presupuestoRepo.save(p);
    }

    // ============================
    // Eliminar
    // ============================
    @Override
    @Transactional
    @Auditable(entidad = "Presupuesto", accion = AccionAuditoria.ELIMINAR, entityId = "#id")
    public void eliminar(Long id) {
        Presupuesto p = presupuestoRepo.findById(id)
                .orElseThrow(() -> new DomainException("Presupuesto no encontrado: " + id));
        presupuestoRepo.delete(p);
    }

    // ============================
    // Actualizar datos generales
    // ============================
    @Override
    @Transactional
    @Auditable(entidad = "Presupuesto", accion = AccionAuditoria.EDITAR, entityId = "#id")
    public void actualizarDatosGenerales(Long id,
                                         String grupoNombre,
                                         String cliente,
                                         String direccion,
                                         String distrito,
                                         String provincia,
                                         String departamento,
                                         LocalDate fechaBase,
                                         BigDecimal jornadaHoras,
                                         String moneda) {

        Presupuesto p = presupuestoRepo.findById(id)
                .orElseThrow(() -> new DomainException("Presupuesto no encontrado: " + id));

        // Grupo (si viene vacío => quitar grupo; si viene texto => upsert por nombre)
        if (grupoNombre != null) {
            String g = grupoNombre.trim();
            if (g.isEmpty()) {
                p.setGrupo(null);
            } else {
                var grupo = grupoRepo.findByNombreIgnoreCase(g)
                        .orElseGet(() -> grupoRepo.save(
                                com.cvanguardistas.billing_service.entities.GrupoPresupuesto.builder()
                                        .nombre(g)
                                        .build()
                        ));
                p.setGrupo(grupo);
            }
        }

        if (cliente != null)       p.setCliente(cliente);
        if (direccion != null)     p.setDireccion(direccion);
        if (distrito != null)      p.setDistrito(distrito);
        if (provincia != null)     p.setProvincia(provincia);
        if (departamento != null)  p.setDepartamento(departamento);
        if (fechaBase != null)     p.setFechaBase(fechaBase);
        if (jornadaHoras != null)  p.setJornadaHoras(jornadaHoras);
        if (moneda != null)        p.setMoneda(moneda);

        presupuestoRepo.save(p);
    }

    // ============================
    // Helpers
    // ============================
    /** Construye el árbol del presupuesto como JsonNode. */
    private JsonNode buildSnapshotJson(Presupuesto p) {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("presupuestoId", p.getId());
            root.put("nombre", p.getNombre());
            root.put("fechaBase", p.getFechaBase());
            root.put("moneda", p.getMoneda());

            List<Map<String, Object>> spsJson = new ArrayList<>();
            for (SubPresupuesto sp : SubPresRepo.findByPresupuestoIdOrderByIdAsc(p.getId())) {
                Map<String, Object> spMap = new LinkedHashMap<>();
                spMap.put("id", sp.getId());
                spMap.put("nombre", sp.getNombre());
                spMap.put("cdTotal", sp.getCdTotal());

                // Asegúrate de tener este método con la S mayúscula en el repo
                List<Partida> arbol = partidaRepo.findArbolBySubPresupuesto(sp.getId());

                List<Map<String, Object>> partidasJson = new ArrayList<>();
                for (Partida part : arbol) {
                    Map<String, Object> partMap = new LinkedHashMap<>();
                    partMap.put("id", part.getId());
                    partMap.put("tipo", part.getTipo());
                    partMap.put("codigo", part.getCodigo());
                    partMap.put("nombre", part.getNombre());
                    partMap.put("metrado", part.getMetrado());
                    partMap.put("cu", part.getCu());
                    partMap.put("parcial", part.getParcial());

                    // Chips
                    List<Map<String, Object>> chipsJson = new ArrayList<>();
                    ptcRepo.findByPartidaId(part.getId()).forEach(ch -> {
                        Map<String, Object> c = new LinkedHashMap<>();
                        c.put("categoriaId", ch.getCategoriaCosto().getId());
                        c.put("categoriaCodigo", ch.getCategoriaCosto().getCodigo());
                        c.put("unitarioCalc", ch.getUnitarioCalc());
                        c.put("totalCalc", ch.getTotalCalc());
                        c.put("usarOverride", ch.getUsarOverride());
                        c.put("unitarioOverride", ch.getUnitarioOverride());
                        chipsJson.add(c);
                    });
                    partMap.put("chips", chipsJson);

                    // Líneas
                    List<Map<String, Object>> lineasJson = new ArrayList<>();
                    partidaInsumoRepo.findByPartidaId(part.getId()).forEach(li -> {
                        Map<String, Object> l = new LinkedHashMap<>();
                        l.put("insumoId", li.getInsumo().getId());
                        l.put("categoriaId", li.getCategoriaCosto().getId());
                        l.put("depende", li.getDependeDeRendimiento());
                        l.put("cantidad", li.getCantidad());
                        l.put("pu", li.getPu());
                        l.put("puCongelado", li.getPuCongelado());
                        l.put("parcial", li.getParcial());
                        lineasJson.add(l);
                    });
                    partMap.put("lineas", lineasJson);

                    partidasJson.add(partMap);
                }

                spMap.put("partidas", partidasJson);
                spsJson.add(spMap);
            }

            root.put("SubPresupuestos", spsJson);
            return objectMapper.valueToTree(root);

        } catch (Exception e) {
            throw new DomainException("Error construyendo snapshot JSON: " + e.getMessage());
        }
    }
}
