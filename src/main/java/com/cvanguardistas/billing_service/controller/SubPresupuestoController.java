// src/main/java/com/cvanguardistas/billing_service/controller/SubPresupuestoController.java
package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.dto.SpInsumoAgregadoDto;
import com.cvanguardistas.billing_service.service.PartidaInsumoService;
import com.cvanguardistas.billing_service.service.SubPresupuestoService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
@RequiredArgsConstructor
public class SubPresupuestoController {

    private final SubPresupuestoService service;
    private final PartidaInsumoService partidaInsumoService; // <-- NUEVO

    // PATCH /api/SubPresupuestos/{id} → renombrar y/o reordenar
    public record PatchSpRequest(String nombre, Integer orden) {}

    @PreAuthorize("@permits.esOwnerDeSubPresupuesto(#id, authentication) or hasRole('ADMIN')")
    @PatchMapping("/SubPresupuestos/{id}")
    public ResponseEntity<Void> patch(@PathVariable @Min(1) Long id,
                                      @RequestBody PatchSpRequest req) {
        service.renombrarYReordenar(id, req.nombre(), req.orden());
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/SubPresupuestos/{id}
    @PreAuthorize("@permits.esOwnerDeSubPresupuesto(#id, authentication) or hasRole('ADMIN')")
    @DeleteMapping("/SubPresupuestos/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ==== NUEVO: listado agregado de insumos del SubPresupuesto ====
    @PreAuthorize("@permits.esOwnerDeSubPresupuesto(#spId, authentication) or hasRole('ADMIN')")
    @GetMapping("/SubPresupuestos/{spId}/insumos")
    public ResponseEntity<List<SpInsumoAgregadoDto>> listarInsumosAgregado(@PathVariable @Min(1) Long spId) {
        var data = partidaInsumoService.listarAgregadoPorSubPresupuesto(spId);
        return ResponseEntity.ok(data);
    }
}
