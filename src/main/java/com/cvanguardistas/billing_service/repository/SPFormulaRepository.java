// src/main/java/com/cvanguardistas/billing_service/repository/SPFormulaRepository.java
package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.SPFormula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SPFormulaRepository extends JpaRepository<SPFormula, Long> {

    List<SPFormula> findBySubPresupuestoIdOrderByOrdenAsc(Long subPresupuestoId);

    Optional<SPFormula> findBySubPresupuestoIdAndVariable(Long subPresupuestoId, String variable);

    @Transactional
    void deleteBySubPresupuestoIdAndVariable(Long subPresupuestoId, String variable);
}
