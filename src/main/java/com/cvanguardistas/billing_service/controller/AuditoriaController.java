// src/main/java/com/cvanguardistas/billing_service/controller/AuditoriaController.java
package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.dto.AuditoriaDto;
import com.cvanguardistas.billing_service.dto.PagedResponse;
import com.cvanguardistas.billing_service.entities.Auditoria;
import com.cvanguardistas.billing_service.repository.AuditoriaRepository;
import com.cvanguardistas.billing_service.service.mapper.AuditoriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaRepository repo;
    private final AuditoriaMapper mapper;

    // GET /api/auditoria/entidad/{entidad}/{id}?page=0&size=20&payload=false
    @GetMapping("/entidad/{entidad}/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PagedResponse<AuditoriaDto> porEntidad(
            @PathVariable String entidad,
            @PathVariable("id") String entidadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean payload
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 200), Sort.by(Sort.Direction.DESC, "creadoEn"));
        Page<Auditoria> pg = repo.findByEntidadAndEntidadId(entidad, entidadId, pageable);
        return mapPage(pg, payload);
    }

    // GET /api/auditoria/usuario/{userId}?page=0&size=20&payload=false
    @GetMapping("/usuario/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public PagedResponse<AuditoriaDto> porUsuario(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean payload
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 200), Sort.by(Sort.Direction.DESC, "creadoEn"));
        Page<Auditoria> pg = repo.findByUsuario_Id(userId, pageable);
        return mapPage(pg, payload);
    }

    // GET /api/auditoria/ultimos?page=0&size=20&payload=false
    @GetMapping("/ultimos")
    @PreAuthorize("hasRole('ADMIN')")
    public PagedResponse<AuditoriaDto> ultimos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean payload
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 200), Sort.by(Sort.Direction.DESC, "creadoEn"));
        Page<Auditoria> pg = repo.findAll(pageable); // ya ordena por pageable
        return mapPage(pg, payload);
    }

    // ---- helpers ----
    private PagedResponse<AuditoriaDto> mapPage(Page<Auditoria> pg, boolean withPayload) {
        Function<Auditoria, AuditoriaDto> toDto = a -> {
            var dto = mapper.toDto(a);
            if (withPayload) return dto; // tal cual
            // Sin payload: devolvemos una copia con payloads nulos
            return new AuditoriaDto(
                    dto.id(), dto.entidad(), dto.entidadId(), dto.accion(),
                    dto.usuarioId(), dto.usuarioNombre(),
                    dto.ip(), dto.userAgent(), dto.correlationId(),
                    dto.creadoEn(), null, null
            );
        };
        var content = pg.map(toDto).getContent();
        // ⚠️ Tu PagedResponse tiene constructor de 5 argumentos (sin 'last')
        return new PagedResponse<>(
                content,
                pg.getNumber(),
                pg.getSize(),
                pg.getTotalElements(),
                pg.getTotalPages()
        );
    }
}
