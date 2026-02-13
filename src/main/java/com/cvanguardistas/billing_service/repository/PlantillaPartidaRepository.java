package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.PlantillaPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantillaPartidaRepository extends JpaRepository<PlantillaPartida, Long> {
    List<PlantillaPartida> findByPlantilla_IdOrderByOrdenAsc(Long plantillaId);
}
