// src/main/java/com/cvanguardistas/billing_service/controller/SPFormulaController.java
package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.dto.PieDto;
import com.cvanguardistas.billing_service.dto.SPFormulaDto;
import com.cvanguardistas.billing_service.dto.UpsertFormulaRequest;
import com.cvanguardistas.billing_service.entities.SPFormula;
import com.cvanguardistas.billing_service.entities.SubPresupuesto;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.SPFormulaRepository;
import com.cvanguardistas.billing_service.repository.SubPresupuestoRepository;
import com.cvanguardistas.billing_service.service.GGItemService;
import com.cvanguardistas.billing_service.service.SPFormulaService;
import com.cvanguardistas.billing_service.service.mapper.SPFormulaMapper;
import com.cvanguardistas.billing_service.web.audit.Auditable;          // <-- NUEVO
import com.cvanguardistas.billing_service.entities.AccionAuditoria;   // <-- NUEVO
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SPFormulaController {

    private final SubPresupuestoRepository spRepo;
    private final SPFormulaRepository formulaRepo;
    private final SPFormulaService formulaService;
    private final GGItemService ggItemService;
    private final SPFormulaMapper formulaMapper;

    // PUT /SubPresupuestos/{spId}/pie/{variable}  (upsert)
    @PutMapping("/SubPresupuestos/{spId}/pie/{variable}")
    @Transactional
    public ResponseEntity<SPFormulaDto> upsertFormula(@PathVariable Long spId,
                                                      @PathVariable String variable,
                                                      @RequestBody @Valid UpsertFormulaRequest req) {
        SubPresupuesto sp = spRepo.findById(spId)
                .orElseThrow(() -> new DomainException("SubPresupuesto no encontrado: " + spId));

        String var = variable.toUpperCase(Locale.ROOT).trim();
        if (var.isBlank()) throw new DomainException("Variable inválida");

        SPFormula f = formulaRepo.findBySubPresupuestoIdAndVariable(spId, var)
                .orElseGet(() -> SPFormula.builder()
                        .subPresupuesto(sp)
                        .variable(var)
                        .build());

        if (req.expresion() != null) f.setExpresion(req.expresion());
        if (req.descripcion() != null) f.setDescripcion(req.descripcion());
        if (req.resaltar() != null) f.setResaltar(req.resaltar());
        if (req.orden() != null) f.setOrden(req.orden());

        f = formulaRepo.save(f);

        // Re-evaluar PIE con CD/GG actuales
        BigDecimal cd = sp.getCdTotal() == null ? BigDecimal.ZERO : sp.getCdTotal();
        BigDecimal gg = ggItemService.recalcularGG(spId, cd);

        Map<String, BigDecimal> base = Map.of("CD", cd, "GG", gg);
        formulaService.evaluarPie(spId, base);

        return ResponseEntity.ok(formulaMapper.toDto(f));
    }

    // GET /SubPresupuestos/{spId}/pie
    @GetMapping("/SubPresupuestos/{spId}/pie")
    @Transactional(readOnly = true)
    public ResponseEntity<PieDto> obtenerPie(@PathVariable Long spId) {
        List<SPFormula> list = formulaRepo.findBySubPresupuestoIdOrderByOrdenAsc(spId);
        return ResponseEntity.ok(new PieDto(formulaMapper.toDtoList(list)));
    }

    // DELETE /SubPresupuestos/{spId}/pie/{variable}
    @DeleteMapping("/SubPresupuestos/{spId}/pie/{variable}")
    @Transactional
    @Auditable(entidad = "SPFormula", accion = AccionAuditoria.ELIMINAR, entityId = "#variable")
    public ResponseEntity<Void> eliminarFormula(@PathVariable Long spId,
                                                @PathVariable String variable) {
        String var = variable.toUpperCase(java.util.Locale.ROOT).trim();
        if (var.isBlank()) throw new DomainException("Variable inválida");

        // Verificar existencia (opcional pero más claro)
        formulaRepo.findBySubPresupuestoIdAndVariable(spId, var)
                .orElseThrow(() -> new DomainException("Fórmula no existe: " + var));

        // Borrar
        formulaRepo.deleteBySubPresupuestoIdAndVariable(spId, var);

        // Re-evaluar PIE con base {CD, GG}
        var sp = spRepo.findById(spId)
                .orElseThrow(() -> new DomainException("SubPresupuesto no encontrado: " + spId));
        BigDecimal cd = sp.getCdTotal() == null ? BigDecimal.ZERO : sp.getCdTotal();
        BigDecimal gg = ggItemService.recalcularGG(spId, cd);

        Map<String, BigDecimal> base = Map.of("CD", cd, "GG", gg);
        formulaService.evaluarPie(spId, base);

        return ResponseEntity.noContent().build();
    }
}
