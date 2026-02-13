package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;
import java.util.List;

/** Para GET /SubPresupuestos/{id}/partidas (árbol jerárquico con totales) */
public record PartidaArbolDto(
        Long id,
        String tipo,
        String nombre,
        Integer orden,
        BigDecimal mo,
        BigDecimal mt,
        BigDecimal eq,
        BigDecimal sc,
        BigDecimal sp,
        BigDecimal cu,
        BigDecimal parcial,
        List<PartidaArbolDto> hijos
) {}
