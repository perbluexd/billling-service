// src/main/java/com/cvanguardistas/billing_service/repository/GGItemDetalleRepository.java
package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.GGItemDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GGItemDetalleRepository extends JpaRepository<GGItemDetalle, Long> {
    List<GGItemDetalle> findByGgItemIdOrderByOrdenAsc(Long ggItemId);
    List<GGItemDetalle> findByGgItem_SubPresupuesto_Id(Long subPresupuestoId);
}
