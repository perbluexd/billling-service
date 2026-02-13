package com.cvanguardistas.billing_service.dto;

import com.cvanguardistas.billing_service.entities.EstadoPresupuesto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PresupuestoDetalleDto(
        Long id,
        String nombre,
        EstadoPresupuesto estado,
        LocalDate fechaBase,
        String moneda,
        BigDecimal jornadaHoras,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn,
        BigDecimal totalCd,                          // suma CD de todos los SubPresupuestos
        List<SubPresupuestoResumenDto> subPresupuestos // resumen de cada SP
) {}
