// src/main/java/com/cvanguardistas/billing_service/repository/PresupuestoSnapshotRepository.java
package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.PresupuestoSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresupuestoSnapshotRepository extends JpaRepository<PresupuestoSnapshot, Long> {

    /** Lista snapshots de un presupuesto, más recientes primero (requiere campo 'creadoEn' en la entidad). */
    List<PresupuestoSnapshot> findByPresupuesto_IdOrderByCreadoEnDesc(Long presupuestoId);

    /** Busca un snapshot por presupuesto y versión para validar duplicados. */
    Optional<PresupuestoSnapshot> findByPresupuesto_IdAndVersion(Long presupuestoId, String version);

    /** Opcional: útil si prefieres borrar en bloque sin cargar a memoria. */
    long deleteByPresupuesto_Id(Long presupuestoId);
}
