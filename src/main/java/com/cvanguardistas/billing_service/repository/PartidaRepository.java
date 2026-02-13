// PartidaRepository.java
package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.Partida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {

    // Árbol por subPresupuesto (JPQL: usa el nombre del *field* Java)
    @Query("""
        SELECT p FROM Partida p
        WHERE p.subPresupuesto.id = :spId
        ORDER BY p.padre.id NULLS FIRST, p.orden
    """)
    List<Partida> findArbolBySubPresupuesto(@Param("spId") Long spId);

    List<Partida> findByPadreOrderByOrdenAsc(Partida padre);

    // Derivado por nombre de propiedad Java (subPresupuesto)
    List<Partida> findBySubPresupuestoIdAndPadreIsNullOrderByOrdenAsc(Long subPresupuestoId);

    // Nuevo: árbol por SP trayendo la UNIDAD con fetch (evita lazy en el Excel)
    @Query("""
        SELECT DISTINCT p
        FROM Partida p
        LEFT JOIN FETCH p.unidad u
        WHERE p.subPresupuesto.id = :spId
        ORDER BY p.padre.id NULLS FIRST, p.orden
    """)
    List<Partida> findArbolWithUnidadBySubPresupuesto(@Param("spId") Long spId);
}
