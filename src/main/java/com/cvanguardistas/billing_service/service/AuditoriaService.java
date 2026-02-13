// src/main/java/com/cvanguardistas/billing_service/service/AuditoriaService.java
package com.cvanguardistas.billing_service.service;

import com.cvanguardistas.billing_service.entities.AccionAuditoria;

public interface AuditoriaService {

    /**
     * Registra un evento de auditoría.
     * @param usuarioId       ID del usuario (puede venir del SecurityContext)
     * @param entidad         Nombre lógico: "Presupuesto", "Partida", etc.
     * @param entidadId       ID del objeto afectado
     * @param accion          CREAR|EDITAR|ELIMINAR|APROBAR|...
     * @param payloadAnterior JSON (puede ser null)
     * @param payloadNuevo    JSON (puede ser null)
     * @param ip              IP remota (puede ser null)
     * @param userAgent       UA (puede ser null)
     * @param correlationId   correlación (puede ser null)
     * @param razonCambio     texto opcional
     */
    void audit(Long usuarioId,
               String entidad,
               String entidadId,
               AccionAuditoria accion,
               String payloadAnterior,
               String payloadNuevo,
               String ip,
               String userAgent,
               String correlationId,
               String razonCambio);
}
