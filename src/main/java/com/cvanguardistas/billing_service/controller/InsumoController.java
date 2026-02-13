package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.dto.ActualizarInsumoRequest;
import com.cvanguardistas.billing_service.dto.CrearInsumoRequest;
import com.cvanguardistas.billing_service.dto.InsumoListItemDto;
import com.cvanguardistas.billing_service.dto.PagedResponse;
import com.cvanguardistas.billing_service.dto.UpdatePrecioBaseRequest;
import com.cvanguardistas.billing_service.service.InsumoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;   // opcional: seguridad a nivel de método
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insumos")
@RequiredArgsConstructor
@Validated
public class InsumoController {

    private final InsumoService insumoService;

    // GET /api/insumos?page=0&size=20&tipo=MO&q=cemento
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<PagedResponse<InsumoListItemDto>> listar(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(insumoService.listar(page, size, tipo, q));
    }

    // POST /api/insumos
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Long> crear(@RequestBody @Valid CrearInsumoRequest req) {
        Long id = insumoService.crear(req);
        return ResponseEntity.ok(id);
    }

    // PATCH /api/insumos/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id,
                                           @RequestBody @Valid ActualizarInsumoRequest req) {
        insumoService.actualizar(id, req);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/insumos/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        insumoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/insumos/{id}/precio
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/precio")
    public ResponseEntity<InsumoListItemDto> actualizarPrecio(
            @PathVariable Long id,
            @RequestBody @Valid UpdatePrecioBaseRequest req
    ) {
        boolean registrarHistorial = Boolean.TRUE.equals(req.registrarHistorial());
        var dto = insumoService.actualizarPrecioBase(id, req.precio(), registrarHistorial);
        return ResponseEntity.ok(dto);
    }
}
