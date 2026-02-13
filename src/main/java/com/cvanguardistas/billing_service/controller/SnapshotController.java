// src/main/java/com/cvanguardistas/billing_service/controller/SnapshotController.java
package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.dto.SnapshotCompareResponse;
import com.cvanguardistas.billing_service.dto.SnapshotSummaryDto;
import com.cvanguardistas.billing_service.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/snapshots")
@RequiredArgsConstructor
public class SnapshotController {

    private final SnapshotService snapshotService;

    @GetMapping("/{presupuestoId}")
    public List<SnapshotSummaryDto> listar(@PathVariable Long presupuestoId) {
        return snapshotService.listar(presupuestoId);
    }

    @GetMapping("/{presupuestoId}/comparar")
    public SnapshotCompareResponse comparar(@PathVariable Long presupuestoId,
                                            @RequestParam("v1") String version1,
                                            @RequestParam("v2") String version2) {
        return snapshotService.comparar(presupuestoId, version1, version2);
    }
}
