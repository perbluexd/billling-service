package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;
import java.util.Map;

public record RecalculoResultDto(
        Long subPresupuestoId,
        BigDecimal cd,
        BigDecimal gg,
        Map<String, BigDecimal> pie // incluye TOTAL si lo tienes configurado
) {}
