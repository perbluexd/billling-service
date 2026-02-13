package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.Insumo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InsumoRepository extends JpaRepository<Insumo, Long>, JpaSpecificationExecutor<Insumo> {

    Optional<Insumo> findByCodigo(String codigo);

    // Utilidades case-insensitive
    Optional<Insumo> findByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCase(String codigo);

    // Búsqueda con filtros (tipo de insumo + texto libre), resultados paginados
    @Query("""
           select i from Insumo i
           left join i.tipoInsumo t
           where (:tipo is null or lower(t.codigo) = :tipo)
             and (:qLower is null or (
                    lower(i.codigo) like concat('%', :qLower, '%')
                 or lower(i.nombre) like concat('%', :qLower, '%')
           ))
           order by i.codigo asc
           """)
    Page<Insumo> searchByTipoAndTexto(@Param("tipo") String tipo,
                                      @Param("qLower") String qLower,
                                      Pageable pageable);
}
