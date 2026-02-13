// src/main/java/com/cvanguardistas/billing_service/repository/CalendarioExcepcionRepository.java
package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.CalendarioExcepcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarioExcepcionRepository extends JpaRepository<CalendarioExcepcion, Long> {
    List<CalendarioExcepcion> findByCalendarioIdOrderByFechaAsc(Long calendarioId);
    void deleteByCalendarioId(Long calendarioId);
}
