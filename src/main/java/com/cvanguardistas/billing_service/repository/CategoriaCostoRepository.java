package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.CategoriaCosto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaCostoRepository extends JpaRepository<CategoriaCosto, Long> {
    Optional<CategoriaCosto> findByCodigo(String codigo);
}