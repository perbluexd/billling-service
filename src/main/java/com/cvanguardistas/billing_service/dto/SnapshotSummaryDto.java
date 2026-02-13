// src/main/java/com/cvanguardistas/billing_service/dto/SnapshotSummaryDto.java
package com.cvanguardistas.billing_service.dto;

import java.time.LocalDateTime;

public record SnapshotSummaryDto(
        Long id,
        String version,
        LocalDateTime creadoEn
) {}
