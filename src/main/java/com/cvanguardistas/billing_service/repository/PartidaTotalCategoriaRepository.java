package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.PartidaTotalCategoria;
import com.cvanguardistas.billing_service.entities.PartidaTotalCategoriaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PartidaTotalCategoriaRepository extends JpaRepository<PartidaTotalCategoria, PartidaTotalCategoriaId> {
    List<PartidaTotalCategoria> findByPartidaId(Long partidaId);
    void deleteByPartidaIdAndCategoriaCostoIdNotIn(Long partidaId, Collection<Long> categorias);
    void deleteByPartidaId(Long partidaId);

}
