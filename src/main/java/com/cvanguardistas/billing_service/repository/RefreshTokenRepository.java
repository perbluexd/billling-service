package com.cvanguardistas.billing_service.repository;

import com.cvanguardistas.billing_service.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // Atado a usuario + no revocado (coincide con tu entidad)
    Optional<RefreshToken> findByUsuario_IdAndTokenHashAndRevocadoFalse(Long usuarioId, String tokenHash);

    List<RefreshToken> findAllByUsuario_IdAndRevocadoFalse(Long usuarioId);

    // Tu campo es OffsetDateTime, así que el parámetro también
    List<RefreshToken> findAllByExpiraEnBefore(OffsetDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshToken r set r.revocado = true where r.usuario.id = :userId and r.revocado = false")
    int revokeAllActiveByUser(@Param("userId") Long userId);
}
