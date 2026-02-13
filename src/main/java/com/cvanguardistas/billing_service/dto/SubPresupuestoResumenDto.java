package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;

public record SubPresupuestoResumenDto(
        Long subPresupuestoId,
        BigDecimal moTotal,
        BigDecimal mtTotal,
        BigDecimal eqTotal,
        BigDecimal scTotal,
        BigDecimal spTotal,
        BigDecimal cdTotal
) {}