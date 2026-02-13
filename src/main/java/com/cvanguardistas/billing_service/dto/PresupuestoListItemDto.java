package com.cvanguardistas.billing_service.dto;

import com.cvanguardistas.billing_service.entities.EstadoPresupuesto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PresupuestoListItemDto(
        Long id,
        String nombre,
        EstadoPresupuesto estado,
        LocalDateTime creadoEn,
        BigDecimal totalCd // suma CD de sus SubPresupuestos (puede ser null -> BigDecimal.ZERO en UI)
) {}
