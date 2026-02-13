// src/main/java/com/cvanguardistas/billing_service/controller/ProgramacionController.java
package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.dto.*;
import com.cvanguardistas.billing_service.service.ProgramacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/programacion")
@RequiredArgsConstructor
public class ProgramacionController {

    private final ProgramacionService svc;

    // ===== Calendarios =====
    @GetMapping("/calendarios")
    public List<CalendarioDto> listarCalendarios() {
        return svc.listarCalendarios();
    }

    @PostMapping("/calendarios")
    public CalendarioDto crearCalendario(@RequestBody @Validated CrearCalendarioRequest r) {
        return svc.crearCalendario(r);
    }

    @DeleteMapping("/calendarios/{id}")
    public ResponseEntity<Void> eliminarCalendario(@PathVariable Long id) {
        svc.eliminarCalendario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/calendarios/{id}/excepciones")
    public List<CalendarioExcepcionDto> listarExcepciones(@PathVariable Long id) {
        return svc.listarExcepciones(id);
    }

    @PostMapping("/calendarios/excepciones")
    public CalendarioExcepcionDto agregarExcepcion(@RequestBody @Validated UpsertCalendarioExcepcionRequest r) {
        return svc.agregarExcepcion(r);
    }

    @DeleteMapping("/calendarios/excepciones/{excepcionId}")
    public ResponseEntity<Void> eliminarExcepcion(@PathVariable Long excepcionId) {
        svc.eliminarExcepcion(excepcionId);
        return ResponseEntity.noContent().build();
    }

    // ===== Tareas =====
    @GetMapping("/{spId}/tareas")
    public ProgramacionTareasDto listarTareas(@PathVariable Long spId) {
        return svc.listarTareas(spId);
    }

    @PostMapping("/tareas")
    public TareaProgramaDto crearTarea(@RequestBody @Validated CrearTareaRequest r) {
        return svc.crearTarea(r);
    }

    @PatchMapping("/tareas/{tareaId}")
    public TareaProgramaDto actualizarTarea(@PathVariable Long tareaId,
                                            @RequestBody @Validated UpdateTareaRequest r) {
        return svc.actualizarTarea(tareaId, r);
    }

    @DeleteMapping("/tareas/{tareaId}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long tareaId) {
        svc.eliminarTarea(tareaId);
        return ResponseEntity.noContent().build();
    }

    // ===== Dependencias =====
    @PostMapping("/dependencias")
    public TareaDependenciaDto crearDependencia(@RequestBody @Validated CrearDependenciaRequest r) {
        return svc.crearDependencia(r);
    }

    @DeleteMapping("/dependencias/{id}")
    public ResponseEntity<Void> eliminarDependencia(@PathVariable Long id) {
        svc.eliminarDependencia(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Export CSV =====
    @GetMapping("/{spId}/export.csv")
    public ResponseEntity<byte[]> exportCsv(@PathVariable Long spId) {
        byte[] data = svc.exportarCsv(spId);
        String fname = URLEncoder.encode("programacion_sp_" + spId + ".csv", StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(fname).build());
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
