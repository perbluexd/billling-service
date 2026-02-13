package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.dto.RecalculoResultDto;
import com.cvanguardistas.billing_service.service.RecalculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recalculo")
@RequiredArgsConstructor
public class RecalculoController {

    private final RecalculoService recalculoService;

    // POST /api/recalculo/SubPresupuestos/{id}
    @PostMapping("/SubPresupuestos/{id}")
    public ResponseEntity<RecalculoResultDto> recalcularSubPresupuesto(@PathVariable Long id) {
        return ResponseEntity.ok(recalculoService.recalcularSubPresupuesto(id));
    }
}
