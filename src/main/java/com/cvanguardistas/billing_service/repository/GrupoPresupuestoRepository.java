package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.GrupoPresupuesto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GrupoPresupuestoRepository extends JpaRepository<GrupoPresupuesto, Long> {
    Optional<GrupoPresupuesto> findByNombreIgnoreCase(String nombre);
}
