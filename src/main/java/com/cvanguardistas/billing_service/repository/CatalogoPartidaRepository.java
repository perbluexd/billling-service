// com.cvanguardistas.billing_service.repository.CatalogoPartidaRepository
package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.PartidaCatalogo;
import com.cvanguardistas.billing_service.entities.TipoPartida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CatalogoPartidaRepository extends JpaRepository<PartidaCatalogo, Long> {

    @Query("""
      SELECT pc FROM PartidaCatalogo pc
       WHERE (:q IS NULL OR LOWER(pc.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(pc.codigo) LIKE LOWER(CONCAT('%', :q, '%')))
         AND (:tipo IS NULL OR pc.tipo = :tipo)
         AND pc.activo = true
      """)
    Page<PartidaCatalogo> buscar(@Param("q") String q,
                                 @Param("tipo") TipoPartida tipo,
                                 Pageable pageable);
    Optional<PartidaCatalogo> findByCodigo(String codigo);

}
