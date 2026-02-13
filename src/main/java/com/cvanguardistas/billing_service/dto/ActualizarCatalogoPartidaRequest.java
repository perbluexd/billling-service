package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;

public record ActualizarCatalogoPartidaRequest(
        String nombre,
        Long unidadId,
        BigDecimal cantidadBase,
        BigDecimal rendimientoBase,
        BigDecimal precioUnitRef,
        Integer orden
) {}
