package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.Presupuesto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    /**
     * Búsqueda paginada con filtros opcionales:
     * - grupo: nombre exacto (case-insensitive). Si null -> todos los grupos.
     * - q: texto libre en nombre o cliente (case-insensitive). Si null -> sin filtro.
     *
     * Requisitos en la entidad Presupuesto:
     *  - Campo 'grupo' (ManyToOne → GrupoPresupuesto) con 'nombre' en la entidad del grupo.
     *  - Campos 'nombre' y 'cliente' en Presupuesto.
     *  - Campo 'creadoEn' (fecha/hora de creación) en Presupuesto.
     */
    @Query("""
select p from Presupuesto p
left join p.grupo g
where (:grupo is null or lower(g.nombre) = :grupo)
  and (:qLower = '' or (
        lower(p.nombre)  like concat('%', :qLower, '%')
     or lower(p.cliente) like concat('%', :qLower, '%')
  ))
order by p.creadoEn desc
""")
    Page<Presupuesto> searchByGrupoAndTexto(
            @Param("grupo") String grupo,
            @Param("qLower") String qLower,
            Pageable pageable);



    /**
     * Devuelve el userId (id del Usuario) que es dueño/creador del Presupuesto.
     * Asume que Presupuesto tiene un campo `creadoPor` (ManyToOne a Usuario).
     *
     * Si tu campo se llama distinto (p.ej. `usuarioCreador` o `owner`),
     * cambia la expresión del select:
     *   select p.usuarioCreador.id from Presupuesto p where p.id = :id
     *   select p.owner.id from Presupuesto p where p.id = :id
     */
    @Query("select p.creadoPor.id from Presupuesto p where p.id = :id")
    Optional<Long> findOwnerIdById(@Param("id") Long id);
}
