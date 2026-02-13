// src/main/java/com/cvanguardistas/billing_service/dto/SnapshotDiffRowDto.java
package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;

public record SnapshotDiffRowDto(
        Long partidaId,
        String codigo,
        String nombre,

        BigDecimal metradoV1,
        BigDecimal metradoV2,
        BigDecimal deltaMetrado,

        BigDecimal cuV1,
        BigDecimal cuV2,
        BigDecimal deltaCu,

        BigDecimal parcialV1,
        BigDecimal parcialV2,
        BigDecimal deltaParcial
) {}
