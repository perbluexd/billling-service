package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.dto.CrearPresupuestoRequest;
import com.cvanguardistas.billing_service.dto.CrearSubPresupuestoRequest;
import com.cvanguardistas.billing_service.dto.PagedResponse;
import com.cvanguardistas.billing_service.dto.PresupuestoDatosGeneralesRequest;
import com.cvanguardistas.billing_service.dto.PresupuestoDetalleDto;
import com.cvanguardistas.billing_service.dto.PresupuestoListItemDto;
import com.cvanguardistas.billing_service.dto.RenombrarPresupuestoRequest;
import com.cvanguardistas.billing_service.dto.SubPresupuestoListItemDto;
import com.cvanguardistas.billing_service.service.PresupuestoService;
import com.cvanguardistas.billing_service.service.SubPresupuestoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presupuestos")
@Validated
@RequiredArgsConstructor
public class PresupuestoController {

    private final PresupuestoService presupuestoService;
    private final SubPresupuestoService subPresupuestoService;

    /** Crear presupuesto en blanco. Cualquier usuario autenticado. */
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<Long> crearEnBlanco(@Valid @RequestBody CrearPresupuestoRequest req) {
        Long id = presupuestoService.crearEnBlanco(
                req.nombre(), req.fechaBase(), req.moneda(), req.jornadaHoras()
        );
        return ResponseEntity.ok(id);
    }

    /** Crear SubPresupuesto (recibe request con nombre). Cualquier usuario autenticado. */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{presupuestoId}/subpresupuestos")
    public ResponseEntity<Long> crearSubPresupuesto(@PathVariable Long presupuestoId,
                                                    @Valid @RequestBody CrearSubPresupuestoRequest req) {
        var creado = subPresupuestoService.crear(presupuestoId, req.nombre());
        return ResponseEntity.ok(creado.getId());
    }

    /** Listar SubPresupuestos por presupuesto. (puedes abrir a isAuthenticated() si lo necesitas) */
    @PreAuthorize("@permits.esOwnerDePresupuesto(#presupuestoId, authentication) or hasRole('ADMIN')")
    @GetMapping("/{presupuestoId}/subpresupuestos")
    public ResponseEntity<List<SubPresupuestoListItemDto>> listarSubPresupuestos(@PathVariable Long presupuestoId) {
        return ResponseEntity.ok(subPresupuestoService.listarPorPresupuesto(presupuestoId));
    }

    /** Aprobar (congelar) presupuesto. Cualquier usuario autenticado. */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/aprobar")
    public ResponseEntity<Void> aprobar(@PathVariable Long id,
                                        @RequestParam(value = "version", required = false) String version) {
        presupuestoService.aprobar(id, version);
        return ResponseEntity.noContent().build();
    }

    /** Listar presupuestos (paginado) con filtros opcionales grupo y q. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<PagedResponse<PresupuestoListItemDto>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String grupo,
            @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(presupuestoService.listar(page, size, grupo, q));
    }

    /** Detalle de presupuesto. Cualquier usuario autenticado. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<PresupuestoDetalleDto> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(presupuestoService.detalle(id));
    }

    /** Renombrar presupuesto. Cualquier usuario autenticado. */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> renombrar(@PathVariable Long id,
                                          @RequestBody @Valid RenombrarPresupuestoRequest req) {
        presupuestoService.renombrar(id, req.nombre());
        return ResponseEntity.noContent().build();
    }

    /** PATCH datos generales (owner o ADMIN). */
    @PreAuthorize("@permits.esOwnerDePresupuesto(#id, authentication) or hasRole('ADMIN')")
    @PatchMapping("/{id}/datos-generales")
    public ResponseEntity<Void> actualizarDatosGenerales(@PathVariable Long id,
                                                         @RequestBody @Valid PresupuestoDatosGeneralesRequest req) {
        presupuestoService.actualizarDatosGenerales(
                id,
                req.grupo(),
                req.cliente(),
                req.direccion(),
                req.distrito(),
                req.provincia(),
                req.departamento(),
                req.fechaBase(),
                req.jornadaHoras(),
                req.moneda()
        );
        return ResponseEntity.noContent().build();
    }

    /** Eliminar presupuesto. Cualquier usuario autenticado. */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        presupuestoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
