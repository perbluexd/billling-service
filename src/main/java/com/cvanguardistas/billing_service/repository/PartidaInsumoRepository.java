package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.PartidaInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartidaInsumoRepository extends JpaRepository<PartidaInsumo, Long> {
    List<PartidaInsumo> findByPartidaId(Long partidaId);
    // NUEVO: para congelar PUs y/o armar snapshot a nivel Presupuesto
    List<PartidaInsumo> findByPartida_SubPresupuesto_Presupuesto_Id(Long presupuestoId);

    // (útil para reportes por SP)
    List<PartidaInsumo> findByPartida_SubPresupuesto_Id(Long subPresupuestoId);

    boolean existsByInsumo_Id(Long insumoId);
    @Query("""
        SELECT li
        FROM PartidaInsumo li
        LEFT JOIN FETCH li.insumo i
        LEFT JOIN FETCH i.unidad iu
        LEFT JOIN FETCH li.categoriaCosto cc
        WHERE li.partida.id = :partidaId
        ORDER BY li.id ASC
    """)
    List<PartidaInsumo> findByPartidaIdFetchAll(@Param("partidaId") Long partidaId);
}