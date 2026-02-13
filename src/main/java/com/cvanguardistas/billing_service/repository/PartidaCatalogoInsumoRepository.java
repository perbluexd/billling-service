// src/main/java/com/cvanguardistas/billing_service/repository/PartidaCatalogoInsumoRepository.java
package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.PartidaCatalogoInsumo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidaCatalogoInsumoRepository extends JpaRepository<PartidaCatalogoInsumo, Long> {
    boolean existsByPartidaCatalogo_IdAndInsumo_IdAndCategoriaCosto_Id(Long partidaCatalogoId,
                                                                       Long insumoId,
                                                                       Long categoriaCostoId);
}
