package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;

public record CatalogoPartidaListItemDto(
        Long id,
        String codigo,
        String nombre,
        String unidadCodigo,
        BigDecimal cantidadBase,
        BigDecimal rendimientoBase,
        BigDecimal precioUnitRef,
        Integer orden
) {}
