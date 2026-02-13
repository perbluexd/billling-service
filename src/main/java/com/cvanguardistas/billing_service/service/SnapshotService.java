// src/main/java/com/cvanguardistas/billing_service/service/SnapshotService.java
package com.cvanguardistas.billing_service.service;

import com.cvanguardistas.billing_service.dto.SnapshotCompareResponse;
import com.cvanguardistas.billing_service.dto.SnapshotSummaryDto;

import java.util.List;

public interface SnapshotService {

    List<SnapshotSummaryDto> listar(Long presupuestoId);

    SnapshotCompareResponse comparar(Long presupuestoId, String version1, String version2);

}
