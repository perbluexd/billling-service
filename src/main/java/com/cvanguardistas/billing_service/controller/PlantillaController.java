// com.cvanguardistas.billing_service.controller.PlantillaController
package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.dto.InstanciarPlantillaRequest;
import com.cvanguardistas.billing_service.dto.PlantillaResumenDto;
import com.cvanguardistas.billing_service.service.PlantillaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plantillas")
@RequiredArgsConstructor
public class PlantillaController {

    private final PlantillaService plantillaService;

    @GetMapping
    public ResponseEntity<List<PlantillaResumenDto>> listar() {
        return ResponseEntity.ok(plantillaService.listar());
    }

    @PostMapping("/{plantillaId}/instanciar")
    public ResponseEntity<Long> instanciar(@PathVariable Long plantillaId,
                                           @RequestBody InstanciarPlantillaRequest req) {
        Long spId = plantillaService.instanciar(plantillaId, req.presupuestoId());
        return ResponseEntity.ok(spId);
    }
}
