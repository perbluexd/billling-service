package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, Long> {
    Optional<Unidad> findByCodigo(String codigo);
    Optional<Unidad> findByCodigoIgnoreCase(String codigo);
}