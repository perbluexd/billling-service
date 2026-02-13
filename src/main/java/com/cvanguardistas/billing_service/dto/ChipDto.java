package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;

public record ChipDto(
        Long categoriaCostoId,
        BigDecimal unitarioCalc,    // por 1 unidad
        BigDecimal totalCalc        // unitarioCalc * metrado
) {}
