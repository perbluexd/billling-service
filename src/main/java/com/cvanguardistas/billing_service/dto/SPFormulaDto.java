// src/main/java/com/cvanguardistas/billing_service/dto/SPFormulaDto.java
package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;

public record SPFormulaDto(
        Long id,
        Long subPresupuestoId,
        String variable,
        String expresion,
        BigDecimal valor,
        Boolean resaltar,
        Integer orden
) {}
