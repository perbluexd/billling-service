// src/main/java/com/cvanguardistas/billing_service/controller/PartidaChipController.java
package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.dto.ChipOverrideRequest;
import com.cvanguardistas.billing_service.dto.HojaDto;
import com.cvanguardistas.billing_service.dto.HojaUpdateCmd;
import com.cvanguardistas.billing_service.service.PartidaChipService;
import com.cvanguardistas.billing_service.service.PartidaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partidas")
@RequiredArgsConstructor
public class PartidaChipController {

    private final PartidaChipService chipService;
    private final PartidaService partidaService;

    // PATCH /api/partidas/{id}/chips/{categoriaId}
    @PatchMapping("/{id}/chips/{categoriaId}")
    public ResponseEntity<HojaDto> setChipOverride(@PathVariable Long id,
                                                   @PathVariable Long categoriaId,
                                                   @RequestBody @Validated ChipOverrideRequest req) {
        chipService.setOverride(id, categoriaId, req.usarOverride(), req.unitarioOverride());

        // Disparamos recálculo completo de la hoja, GG y Pie
        HojaUpdateCmd recalc = new HojaUpdateCmd(id, null, null, null, null, null, null);
        HojaDto dto = partidaService.actualizarHoja(recalc);
        return ResponseEntity.ok(dto);
    }
}
