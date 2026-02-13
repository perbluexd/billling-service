// src/main/java/com/cvanguardistas/billing_service/dto/AuditoriaDto.java
package com.cvanguardistas.billing_service.dto;

import com.cvanguardistas.billing_service.entities.AccionAuditoria;

import java.time.LocalDateTime;

public record AuditoriaDto(
        Long id,
        String entidad,
        String entidadId,
        AccionAuditoria accion,
        Long usuarioId,
        String usuarioNombre,     // ajusta a tu campo real (nombre, email, username)
        String ip,
        String userAgent,
        String correlationId,
        LocalDateTime creadoEn,
        String payloadAnterior,   // opcional (se puede devolver null según flag)
        String payloadNuevo       // opcional (se puede devolver null según flag)
) {}
