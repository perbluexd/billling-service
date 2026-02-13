// src/main/java/com/cvanguardistas/billing_service/dto/SubPresupuestoListItemDto.java
package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;

public record SubPresupuestoListItemDto(
        Long id,
        String nombre,
        Integer orden,
        BigDecimal moTotal,
        BigDecimal mtTotal,
        BigDecimal eqTotal,
        BigDecimal scTotal,
        BigDecimal spTotal,
        BigDecimal cdTotal,
        BigDecimal ggTotal,   // calculado al vuelo (nullable)
        BigDecimal pieTotal   // calculado al vuelo (nullable)
) {}
