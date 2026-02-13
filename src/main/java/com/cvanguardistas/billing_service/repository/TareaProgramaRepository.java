// src/main/java/com/cvanguardistas/billing_service/repository/TareaProgramaRepository.java
package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.TareaPrograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaProgramaRepository extends JpaRepository<TareaPrograma, Long> {
    List<TareaPrograma> findBySubPresupuestoIdOrderByOrdenAsc(Long subPresupuestoId);
    List<TareaPrograma> findByCalendarioId(Long calendarioId);
}
