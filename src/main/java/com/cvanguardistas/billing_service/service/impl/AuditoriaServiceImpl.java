// src/main/java/com/cvanguardistas/billing_service/service/impl/AuditoriaServiceImpl.java
package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.entities.*;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.AuditoriaRepository;
import com.cvanguardistas.billing_service.repository.UsuarioRepository;
import com.cvanguardistas.billing_service.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository auditoriaRepo;
    private final UsuarioRepository usuarioRepo;

    /**
     * Se ejecuta en una NUEVA transacción para no depender de la tx del caso de uso.
     * Esto permite grabar incluso si el persist se hace en afterCommit del método principal.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void audit(Long usuarioId,
                      String entidad,
                      String entidadId,
                      AccionAuditoria accion,
                      String payloadAnterior,
                      String payloadNuevo,
                      String ip,
                      String userAgent,
                      String correlationId,
                      String razonCambio) {

        if (usuarioId == null) {
            throw new DomainException("Usuario nulo en auditoría");
        }

        Usuario user = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new DomainException("Usuario no encontrado: " + usuarioId));

        // La columna entidad_id es NOT NULL en la entidad; evitamos NPE.
        String safeEntidadId = (entidadId == null) ? "" : entidadId;

        Auditoria aud = Auditoria.builder()
                .usuario(user)
                .entidad(entidad)
                .entidadId(safeEntidadId)
                .accion(accion)
                .payloadAnterior(payloadAnterior)
                .payloadNuevo(payloadNuevo)
                .ip(ip)
                .userAgent(userAgent)
                .correlationId(correlationId)
                .razonCambio(razonCambio)
                .build();

        auditoriaRepo.save(aud);
    }
}
