package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.TipoInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoInsumoRepository extends JpaRepository<TipoInsumo, Long> {
    Optional<TipoInsumo> findByCodigo(String codigo);
    Optional<TipoInsumo> findByCodigoIgnoreCase(String codigo);
}
