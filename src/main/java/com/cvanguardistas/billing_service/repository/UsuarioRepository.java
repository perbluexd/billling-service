package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    @Query("""
           select r.nombre from UsuarioRol ur 
             join ur.rol r 
            where ur.usuario.id = :userId
           """)
    List<String> findRolesByUsuarioId(Long userId);
}
