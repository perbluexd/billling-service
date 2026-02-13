package com.cvanguardistas.billing_service.service;

import com.cvanguardistas.billing_service.dto.SubPresupuestoListItemDto;
import com.cvanguardistas.billing_service.dto.SubPresupuestoResumenDto;
import com.cvanguardistas.billing_service.entities.SubPresupuesto;

import java.util.List;

public interface SubPresupuestoService {

    // --- Firma legacy (compatibilidad con APIs ya probadas)
    SubPresupuesto crear(Long presupuestoId, String nombre);

    // --- Firma nueva (DTO completo, listas MUTABLES, etc.)
    SubPresupuesto crear(Long presupuestoId, SubPresupuesto dto);

    List<SubPresupuestoListItemDto> listarPorPresupuesto(Long presupuestoId);

    void renombrarYReordenar(Long spId, String nombre, Integer nuevoOrden);

    void eliminar(Long spId);

    SubPresupuestoResumenDto resumen(Long subPresupuestoId);

    void recalcularTotales(Long subPresupuestoId);
}
