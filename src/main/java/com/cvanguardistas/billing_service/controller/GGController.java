// src/main/java/com/cvanguardistas/billing_service/controller/GGController.java
package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.dto.*;
import com.cvanguardistas.billing_service.entities.GGItem;
import com.cvanguardistas.billing_service.entities.GGItemDetalle;
import com.cvanguardistas.billing_service.entities.SubPresupuesto;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.GGItemDetalleRepository;
import com.cvanguardistas.billing_service.repository.GGItemRepository;
import com.cvanguardistas.billing_service.repository.SubPresupuestoRepository;
import com.cvanguardistas.billing_service.service.GGItemService;
import com.cvanguardistas.billing_service.service.SPFormulaService;
import com.cvanguardistas.billing_service.service.mapper.GGItemDetalleMapper;
import com.cvanguardistas.billing_service.service.mapper.GGItemMapper;
import com.cvanguardistas.billing_service.web.audit.Auditable;               // <-- NUEVO
import com.cvanguardistas.billing_service.entities.AccionAuditoria;        // <-- NUEVO
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GGController {

    private final SubPresupuestoRepository spRepo;
    private final GGItemRepository itemRepo;
    private final GGItemDetalleRepository detRepo;

    private final GGItemService ggItemService;
    private final SPFormulaService spFormulaService;

    private final GGItemMapper itemMapper;
    private final GGItemDetalleMapper detMapper;

    // POST /SubPresupuestos/{spId}/gg/items
    @PostMapping("/SubPresupuestos/{spId}/gg/items")
    public ResponseEntity<GGItemDto> crearItem(@PathVariable Long spId,
                                               @RequestBody @Validated CrearGGItemRequest req) {
        SubPresupuesto sp = spRepo.findById(spId)
                .orElseThrow(() -> new DomainException("SubPresupuesto no encontrado: " + spId));

        GGItem item = GGItem.builder()
                .subPresupuesto(sp)
                .tipo(req.tipo())
                .formato(req.formato())
                .titulo(req.titulo())
                .orden(req.orden())
                .build();

        item = itemRepo.save(item);
        return ResponseEntity.ok(itemMapper.toDto(item));
    }

    // POST /gg/items/{itemId}/detalles
    @PostMapping("/gg/items/{itemId}/detalles")
    @Transactional
    public ResponseEntity<GGItemDetalleDto> crearDetalle(@PathVariable Long itemId,
                                                         @RequestBody @Validated CrearGGItemDetalleRequest req) {
        GGItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new DomainException("GGItem no encontrado: " + itemId));

        GGItemDetalle d = GGItemDetalle.builder()
                .ggItem(item)
                .descripcion(req.descripcion())
                .unidad(req.unidad())
                .cantidadDesc(req.cantidadDesc())
                .cantidad(req.cantidad())
                .precio(req.precio())
                .porcentaje(req.porcentaje())
                .orden(req.orden())
                .build();

        d = detRepo.save(d);

        // Recalcular GG y Pie
        BigDecimal cd = item.getSubPresupuesto().getCdTotal() == null
                ? BigDecimal.ZERO : item.getSubPresupuesto().getCdTotal();
        BigDecimal gg = ggItemService.recalcularGG(item.getSubPresupuesto().getId(), cd);

        Map<String, BigDecimal> base = new HashMap<>();
        base.put("CD", cd);
        base.put("GG", gg);
        spFormulaService.evaluarPie(item.getSubPresupuesto().getId(), base);

        return ResponseEntity.ok(detMapper.toDto(d));
    }

    // PATCH /gg/detalles/{detalleId}
    @PatchMapping("/gg/detalles/{detalleId}")
    @Transactional
    public ResponseEntity<GGItemDetalleDto> editarDetalle(@PathVariable Long detalleId,
                                                          @RequestBody @Validated EditarGGItemDetalleRequest req) {
        GGItemDetalle d = detRepo.findById(detalleId)
                .orElseThrow(() -> new DomainException("Detalle GG no encontrado: " + detalleId));

        if (req.descripcion() != null) d.setDescripcion(req.descripcion());
        if (req.unidad() != null) d.setUnidad(req.unidad());
        if (req.cantidadDesc() != null) d.setCantidadDesc(req.cantidadDesc());
        if (req.cantidad() != null) d.setCantidad(req.cantidad());
        if (req.precio() != null) d.setPrecio(req.precio());
        if (req.porcentaje() != null) d.setPorcentaje(req.porcentaje());
        if (req.orden() != null) d.setOrden(req.orden());

        d = detRepo.save(d);

        // Recalcular GG y Pie
        Long spId = d.getGgItem().getSubPresupuesto().getId();
        SubPresupuesto sp = spRepo.findById(spId)
                .orElseThrow(() -> new DomainException("SubPresupuesto no encontrado: " + spId));
        BigDecimal cd = sp.getCdTotal() == null ? BigDecimal.ZERO : sp.getCdTotal();
        BigDecimal gg = ggItemService.recalcularGG(spId, cd);

        Map<String, BigDecimal> base = new HashMap<>();
        base.put("CD", cd);
        base.put("GG", gg);
        spFormulaService.evaluarPie(spId, base);

        return ResponseEntity.ok(detMapper.toDto(d));
    }

    // ===========================
    // NUEVOS: DELETE con auditoría
    // ===========================

    // DELETE /api/gg/items/{itemId}
    @DeleteMapping("/gg/items/{itemId}")
    @Transactional
    @Auditable(entidad = "GGItem", accion = AccionAuditoria.ELIMINAR, entityId = "#itemId")
    public ResponseEntity<Void> eliminarItem(@PathVariable Long itemId) {
        GGItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new DomainException("GGItem no encontrado: " + itemId));

        // Borrar detalles primero (no hay cascade en entidad)
        var detalles = detRepo.findByGgItemIdOrderByOrdenAsc(itemId);
        detRepo.deleteAll(detalles);

        // Luego borrar el item
        itemRepo.delete(item);

        // Recalcular GG y PIE del SP
        Long spId = item.getSubPresupuesto().getId();
        SubPresupuesto sp = spRepo.findById(spId)
                .orElseThrow(() -> new DomainException("SubPresupuesto no encontrado: " + spId));
        BigDecimal cd = sp.getCdTotal() == null ? BigDecimal.ZERO : sp.getCdTotal();
        BigDecimal gg = ggItemService.recalcularGG(spId, cd);
        Map<String, BigDecimal> base = new HashMap<>();
        base.put("CD", cd);
        base.put("GG", gg);
        spFormulaService.evaluarPie(spId, base);

        return ResponseEntity.noContent().build();
    }

    // DELETE /api/gg/detalles/{detalleId}
    @DeleteMapping("/gg/detalles/{detalleId}")
    @Transactional
    @Auditable(entidad = "GGItemDetalle", accion = AccionAuditoria.ELIMINAR, entityId = "#detalleId")
    public ResponseEntity<Void> eliminarDetalle(@PathVariable Long detalleId) {
        GGItemDetalle d = detRepo.findById(detalleId)
                .orElseThrow(() -> new DomainException("Detalle GG no encontrado: " + detalleId));

        Long spId = d.getGgItem().getSubPresupuesto().getId();
        detRepo.delete(d);

        // Recalcular GG y PIE del SP
        SubPresupuesto sp = spRepo.findById(spId)
                .orElseThrow(() -> new DomainException("SubPresupuesto no encontrado: " + spId));
        BigDecimal cd = sp.getCdTotal() == null ? BigDecimal.ZERO : sp.getCdTotal();
        BigDecimal gg = ggItemService.recalcularGG(spId, cd);
        Map<String, BigDecimal> base = new HashMap<>();
        base.put("CD", cd);
        base.put("GG", gg);
        spFormulaService.evaluarPie(spId, base);

        return ResponseEntity.noContent().build();
    }
}
