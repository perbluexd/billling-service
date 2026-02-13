// src/main/java/com/cvanguardistas/billing_service/repository/TareaDependenciaRepository.java
package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.TareaDependencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaDependenciaRepository extends JpaRepository<TareaDependencia, Long> {
    List<TareaDependencia> findBySucesoraId(Long sucesoraId);
    List<TareaDependencia> findByPredecesoraId(Long predecesoraId);
    void deleteByPredecesoraIdOrSucesoraId(Long predecesoraId, Long sucesoraId);
    List<TareaDependencia> findBySucesora_SubPresupuesto_Id(Long spId);
}
