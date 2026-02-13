// src/main/java/com/cvanguardistas/billing_service/repository/CalendarioRepository.java
package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.Calendario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendarioRepository extends JpaRepository<Calendario, Long> {
}
