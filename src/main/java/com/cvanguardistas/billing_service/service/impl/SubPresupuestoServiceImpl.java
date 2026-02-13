// src/main/java/com/cvanguardistas/billing_service/service/impl/SubPresupuestoServiceImpl.java
package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.dto.SubPresupuestoListItemDto;
import com.cvanguardistas.billing_service.dto.SubPresupuestoResumenDto;
import com.cvanguardistas.billing_service.entities.*;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.PartidaRepository;
import com.cvanguardistas.billing_service.repository.PresupuestoRepository;
import com.cvanguardistas.billing_service.repository.SubPresupuestoRepository;
import com.cvanguardistas.billing_service.service.GGItemService;
import com.cvanguardistas.billing_service.service.SPFormulaService;
import com.cvanguardistas.billing_service.service.SubPresupuestoService;
import com.cvanguardistas.billing_service.service.mapper.SubPresupuestoMapper;
import com.cvanguardistas.billing_service.web.audit.Auditable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubPresupuestoServiceImpl implements SubPresupuestoService {

    private final PresupuestoRepository presupuestoRepo;
    private final SubPresupuestoRepository SubPresupuestoRepo;
    private final PartidaRepository partidaRepo;
    private final SubPresupuestoMapper mapper;

    // Para GG y Pie on-demand
    private final GGItemService ggItemService;
    private final SPFormulaService spFormulaService;

    // =================================================================================
    // CREAR (firma legacy) -> delega a la nueva
    // =================================================================================
    @Override
    @Transactional
    public SubPresupuesto crear(Long presupuestoId, String nombre) {
        SubPresupuesto dto = new SubPresupuesto();
        dto.setNombre(nombre);
        // colecciones se crearán/llenarán en el método nuevo mediante helpers
        return crear(presupuestoId, dto);
    }

    // =================================================================================
    // CREAR (firma nueva): usar helpers y NO reemplazar colecciones
    // =================================================================================
    @Override
    @Transactional
    public SubPresupuesto crear(Long presupuestoId, SubPresupuesto dto) {
        // 1) Cargar el presupuesto padre
        Presupuesto padre = presupuestoRepo.findById(presupuestoId)
                .orElseThrow(() -> new DomainException("Presupuesto no encontrado: " + presupuestoId));

        // 2) Construir la entidad base (sin tocar listas aún)
        SubPresupuesto entity = new SubPresupuesto();
        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setOrden(dto.getOrden());

        // 3) Amarrar al padre usando el helper del padre (no reemplaza lista)
        padre.addSubPresupuesto(entity); // setea presupuesto en entity y lo agrega a la colección

        // 4) Agregar hijos usando helpers (garantiza bidireccionalidad)
        if (dto.getPartidas() != null) {
            for (Partida p : dto.getPartidas()) {
                entity.addPartida(p);
            }
        }
        if (dto.getFormulas() != null) {
            for (SPFormula f : dto.getFormulas()) {
                entity.addFormula(f);
            }
        }

        // 5) Persistir (cualquiera funciona: guardar entity o padre; dejamos entity)
        entity = SubPresupuestoRepo.save(entity);

        // Opcional: guardar padre también es seguro por cascada, pero no necesario aquí
        // presupuestoRepo.save(padre);

        return entity;
    }

    // =================================================================================
    // LISTAR por presupuesto con GG y PIE on-demand
    // =================================================================================
    @Override
    @Transactional(readOnly = true)
    public List<SubPresupuestoListItemDto> listarPorPresupuesto(Long presupuestoId) {
        presupuestoRepo.findById(presupuestoId)
                .orElseThrow(() -> new DomainException("Presupuesto no encontrado: " + presupuestoId));

        var list = SubPresupuestoRepo.findByPresupuestoIdOrderByIdAsc(presupuestoId);

        return list.stream().map(sp -> {
            BigDecimal gg = null;
            BigDecimal pie = null;
            try {
                BigDecimal cd = sp.getCdTotal() == null ? BigDecimal.ZERO : sp.getCdTotal();
                gg = ggItemService.recalcularGG(sp.getId(), cd);

                // Evaluar fórmulas del “pie”; variables: CD, GG
                var vars = spFormulaService.evaluarPie(sp.getId(), Map.of("CD", cd, "GG", gg));
                if (vars.containsKey("TOTAL")) {
                    pie = vars.get("TOTAL");
                } else if (vars.containsKey("PIE")) {
                    pie = vars.get("PIE");
                }
            } catch (Exception ignore) { /* mantenemos null si algo falla */ }

            return new SubPresupuestoListItemDto(
                    sp.getId(),
                    sp.getNombre(),
                    sp.getOrden(),
                    sp.getMoTotal(),
                    sp.getMtTotal(),
                    sp.getEqTotal(),
                    sp.getScTotal(),
                    sp.getSpTotal(),
                    sp.getCdTotal(),
                    gg,
                    pie
            );
        }).toList();
    }

    // =================================================================================
    // RENOMBRAR + REORDENAR
    // =================================================================================
    @Override
    @Transactional
    @Auditable(entidad = "SubPresupuesto", accion = AccionAuditoria.EDITAR, entityId = "#spId")
    public void renombrarYReordenar(Long spId, String nombre, Integer nuevoOrden) {
        SubPresupuesto sp = SubPresupuestoRepo.findById(spId)
                .orElseThrow(() -> new DomainException("SubPresupuesto no encontrado: " + spId));

        if (nombre != null && !nombre.isBlank()) sp.setNombre(nombre.trim());
        if (nuevoOrden != null) sp.setOrden(nuevoOrden);

        SubPresupuestoRepo.save(sp);
    }

    // =================================================================================
    // ELIMINAR con validaciones
    // =================================================================================
    @Override
    @Transactional
    @Auditable(entidad = "SubPresupuesto", accion = AccionAuditoria.ELIMINAR, entityId = "#spId")
    public void eliminar(Long spId) {
        SubPresupuesto sp = SubPresupuestoRepo.findById(spId)
                .orElseThrow(() -> new DomainException("SubPresupuesto no encontrado: " + spId));

        // 1) Presupuesto aprobado no permite eliminar SP
        if (sp.getPresupuesto() != null && sp.getPresupuesto().getEstado() == EstadoPresupuesto.APROBADO) {
            throw new DomainException("No se puede eliminar un SubPresupuesto de un presupuesto APROBADO");
        }

        // 2) Validación de hijos (opción segura: exigir que esté vacío)
        boolean tienePartidas = !partidaRepo.findArbolBySubPresupuesto(spId).isEmpty();
        if (tienePartidas) {
            throw new DomainException("El SubPresupuesto tiene partidas; elimínalas primero.");
        }

        // Si usas repos de GG/Formulas/Programación, valida también (descomenta si aplica):
        // boolean tieneGG = ggItemRepo.existsBySubPresupuesto_Id(spId);
        // boolean tieneFormulas = formulaRepo.existsBySubPresupuestoId(spId);
        // boolean tieneTareas = tareaProgramaRepo.existsBySubPresupuestoId(spId);
        // if (tieneGG || tieneFormulas || tieneTareas) throw new DomainException("Elimina primero GG/Fórmulas/Programación.");

        SubPresupuestoRepo.delete(sp);
    }

    // =================================================================================
    // STUBS (si los estás llamando desde el controller, impleméntalos o quita la llamada)
    // =================================================================================
    @Override
    public SubPresupuestoResumenDto resumen(Long subPresupuestoId) {
        throw new UnsupportedOperationException("resumen() no implementado aún");
    }

    @Override
    public void recalcularTotales(Long subPresupuestoId) {
        throw new UnsupportedOperationException("recalcularTotales() no implementado aún");
    }
}
