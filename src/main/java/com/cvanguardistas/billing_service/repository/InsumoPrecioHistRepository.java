package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.InsumoPrecioHist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface InsumoPrecioHistRepository extends JpaRepository<InsumoPrecioHist, Long> {

    // último precio cargado para un insumo
    Optional<InsumoPrecioHist> findTopByInsumo_IdOrderByVigenteDesdeDesc(Long insumoId);

    // precio vigente a una fecha-hora dada
    Optional<InsumoPrecioHist>
    findTopByInsumo_IdAndVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(
            Long insumoId,
            java.time.LocalDateTime fechaHora
    );
}