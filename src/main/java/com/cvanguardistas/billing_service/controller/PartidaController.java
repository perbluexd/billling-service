// src/main/java/com/cvanguardistas/billing_service/controller/PartidaController.java
package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.dto.CrearPartidaRequest;
import com.cvanguardistas.billing_service.dto.HojaDto;
import com.cvanguardistas.billing_service.dto.HojaUpdateCmd;
import com.cvanguardistas.billing_service.dto.PartidaArbolDto;
import com.cvanguardistas.billing_service.service.PartidaInsumoService;
import com.cvanguardistas.billing_service.service.PartidaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PartidaController {

    private final PartidaService partidaService;
    private final PartidaInsumoService partidaInsumoService;

    // DTO ultracorto interno para mover
    public record MoverPartidaRequest(Long padreId, Integer orden) {}

    // POST /partidas → crear TÍTULO/SubTÍTULO/HOJA
    @PostMapping("/partidas")
    public ResponseEntity<Long> crear(@RequestBody @Validated CrearPartidaRequest req) {
        Long id = partidaService.crear(
                req.subPresupuestoId(),
                req.padreId(),
                req.tipo(),
                req.codigo(),
                req.nombre(),
                req.unidadId(),
                req.rendimiento(),
                req.metrado(),
                req.orden()
        );
        return ResponseEntity.ok(id);
    }

    // PATCH /partidas/{id} → actualizar HOJA (metrado/rendimiento/unidad/padre/orden/líneas)
    @PatchMapping("/partidas/{id}")
    public ResponseEntity<HojaDto> actualizarHoja(@PathVariable Long id,
                                                  @RequestBody @Validated HojaUpdateCmd cmd) {
        // Forzamos que el ID de la ruta sea el usado por el servicio
        HojaUpdateCmd cmdConId = new HojaUpdateCmd(
                id,
                cmd.metrado(),
                cmd.rendimiento(),
                cmd.unidadId(),
                cmd.padreId(),
                cmd.orden(),
                cmd.lineas()
        );
        return ResponseEntity.ok(partidaService.actualizarHoja(cmdConId));
    }

    // PATCH /partidas/{id}/mover  body: { padreId, orden }
    @PatchMapping("/partidas/{id}/mover")
    public ResponseEntity<Void> mover(@PathVariable Long id,
                                      @RequestBody MoverPartidaRequest req) {
        partidaService.moverPartida(id, req.padreId(), req.orden());
        return ResponseEntity.noContent().build();
    }

    // GET /SubPresupuestos/{spId}/partidas → árbol
    @GetMapping("/SubPresupuestos/{spId}/partidas")
    public ResponseEntity<List<PartidaArbolDto>> obtenerArbol(@PathVariable Long spId) {
        List<PartidaArbolDto> arbol = partidaService.obtenerArbol(spId);
        return ResponseEntity.ok(arbol);
    }

    // POST /SubPresupuestos/{spId}/partidas/from-catalog/{catalogoPartidaId} → Instanciar hoja desde catálogo
    @PostMapping("/SubPresupuestos/{spId}/partidas/from-catalog/{catalogoPartidaId}")
    public ResponseEntity<Long> instanciarDesdeCatalogo(@PathVariable Long spId,
                                                        @PathVariable Long catalogoPartidaId) {
        Long id = partidaService.instanciarDesdeCatalogo(spId, catalogoPartidaId);
        return ResponseEntity.ok(id);
    }

    // GET /partidas/{id}/insumos → listar líneas ACU
    @GetMapping("/partidas/{id}/insumos")
    public ResponseEntity<?> listarInsumos(@PathVariable Long id) {
        return ResponseEntity.ok(partidaInsumoService.listar(id));
    }

    // POST /partidas/{id}/insumos → agregar línea
    @PostMapping("/partidas/{id}/insumos")
    public ResponseEntity<?> agregarInsumo(@PathVariable Long id,
                                           @RequestBody @Validated com.cvanguardistas.billing_service.dto.LineaACURequest req) {
        return ResponseEntity.ok(partidaInsumoService.agregar(id, req));
    }

    // PATCH /partidas/insumos/{lineaId} → editar línea
    @PatchMapping("/partidas/insumos/{lineaId}")
    public ResponseEntity<?> editarInsumo(@PathVariable Long lineaId,
                                          @RequestBody @Validated com.cvanguardistas.billing_service.dto.LineaACURequest req) {
        return ResponseEntity.ok(partidaInsumoService.editar(lineaId, req));
    }

    // DELETE /partidas/insumos/{lineaId} → borra y devuelve la Hoja recalculada (mejor UX)
    @DeleteMapping("/partidas/insumos/{lineaId}")
    public ResponseEntity<HojaDto> eliminarInsumo(@PathVariable Long lineaId) {
        return ResponseEntity.ok(partidaInsumoService.eliminarYRecalcular(lineaId));
    }

    @DeleteMapping("/partidas/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        partidaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
