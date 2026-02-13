package com.cvanguardistas.billing_service.dto;

import com.cvanguardistas.billing_service.entities.TipoPartida;

import java.math.BigDecimal;

public record CrearPartidaRequest(
        Long subPresupuestoId,
        Long padreId,              // null si es TÍTULO raíz
        TipoPartida tipo,          // TITULO | SubTITULO | HOJA
        String codigo,
        String nombre,
        Long unidadId,             // requerido si HOJA
        BigDecimal rendimiento,    // requerido si HOJA
        BigDecimal metrado,        // requerido si HOJA
        Integer orden              // opcional
) {}
