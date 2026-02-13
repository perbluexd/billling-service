package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.SubPresupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubPresupuestoRepository extends JpaRepository<SubPresupuesto, Long> {
    long countByPresupuestoId(Long presupuestoId);
    List<SubPresupuesto> findByPresupuestoIdOrderByIdAsc(Long presupuestoId);
}
