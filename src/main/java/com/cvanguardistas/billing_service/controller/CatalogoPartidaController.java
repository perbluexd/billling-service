package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.dto.*;
import com.cvanguardistas.billing_service.service.CatalogoPartidaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalogo/partidas")
@RequiredArgsConstructor
@Validated
public class CatalogoPartidaController {

    private final CatalogoPartidaService catalogoPartidaService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{plantillaId}")
    public ResponseEntity<List<CatalogoPartidaListItemDto>> listar(@PathVariable Long plantillaId) {
        return ResponseEntity.ok(catalogoPartidaService.listarPorPlantilla(plantillaId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Long> crear(@RequestBody @Validated CrearCatalogoPartidaRequest req) {
        Long id = catalogoPartidaService.crear(req);
        return ResponseEntity.ok(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id,
                                           @RequestBody @Validated ActualizarCatalogoPartidaRequest req) {
        catalogoPartidaService.actualizar(id, req);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        catalogoPartidaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
