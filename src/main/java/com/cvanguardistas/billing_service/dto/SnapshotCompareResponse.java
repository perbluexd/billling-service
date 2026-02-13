package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;
import java.util.List;

public record SnapshotCompareResponse(
        Long presupuestoId,
        String version1,
        String version2,
        List<SnapshotDiffRowDto> diferencias,
        BigDecimal totalParcialV1,
        BigDecimal totalParcialV2,
        BigDecimal deltaParcialTotal
) {}
