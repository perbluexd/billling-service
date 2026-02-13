// src/main/java/com/cvanguardistas/billing_service/repository/GGItemRepository.java
package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.GGItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GGItemRepository extends JpaRepository<GGItem, Long> {
    List<GGItem> findBySubPresupuestoIdOrderByOrdenAsc(Long subPresupuestoId);
}
