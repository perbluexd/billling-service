// src/main/java/com/cvanguardistas/billing_service/controller/ReporteController.java
package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    // EXISTENTE: desagregado por líneas/partidas
    @GetMapping("/{spId}/desagregado.xlsx")
    public ResponseEntity<byte[]> exportar(@PathVariable Long spId) {
        byte[] data = reporteService.exportarDesagregadoXlsx(spId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition
                .attachment()
                .filename("desagregado_sp_" + spId + ".xlsx")
                .build());
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        return ResponseEntity.ok().headers(headers).body(data);
    }

    // NUEVO: insumos agregados del SubPresupuesto
    @PreAuthorize("@permits.esOwnerDeSubPresupuesto(#spId, authentication) or hasRole('ADMIN')")
    @GetMapping("/{spId}/insumos.xlsx")
    public ResponseEntity<byte[]> exportarInsumos(@PathVariable Long spId) {
        byte[] data = reporteService.exportarInsumosXlsx(spId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition
                .attachment()
                .filename("insumos_sp_" + spId + ".xlsx")
                .build());
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        return ResponseEntity.ok().headers(headers).body(data);
    }
}
