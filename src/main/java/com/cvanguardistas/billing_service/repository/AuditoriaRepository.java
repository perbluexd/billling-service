// src/main/java/com/cvanguardistas/billing_service/repository/AuditoriaRepository.java
package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    // Consultar por entidad+objeto (ordenado por fecha desc)
    List<Auditoria> findByEntidadAndEntidadIdOrderByCreadoEnDesc(String entidad, String entidadId);

    // Consultar por usuario (ordenado por fecha desc)
    List<Auditoria> findByUsuario_IdOrderByCreadoEnDesc(Long usuarioId);

    // Últimos N (útil para dashboard)
    List<Auditoria> findTop100ByOrderByCreadoEnDesc();

    // --- Opcionales (paginadas / por rango de fechas) ---
    Page<Auditoria> findByEntidadAndEntidadId(String entidad, String entidadId, Pageable pageable);
    Page<Auditoria> findByUsuario_Id(Long usuarioId, Pageable pageable);
    Page<Auditoria> findByCreadoEnBetween(LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
}
