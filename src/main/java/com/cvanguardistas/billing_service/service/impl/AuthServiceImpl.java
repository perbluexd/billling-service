package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.dto.AuthTokensResponse;
import com.cvanguardistas.billing_service.entities.RefreshToken;
import com.cvanguardistas.billing_service.entities.Usuario;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.RefreshTokenRepository;
import com.cvanguardistas.billing_service.repository.UsuarioRepository;
import com.cvanguardistas.billing_service.security.JwtProperties;
import com.cvanguardistas.billing_service.security.JwtService;
import com.cvanguardistas.billing_service.service.AuthService;
import com.cvanguardistas.billing_service.util.HashUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepo;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties props;

    @Override
    @Transactional
    public AuthTokensResponse login(String email, String rawPassword, String ip, String userAgent) {
        Usuario u = usuarioRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new DomainException("Credenciales inválidas"));

        if (Boolean.FALSE.equals(u.getActivo()))
            throw new DomainException("Usuario inactivo");

        if (u.getHashPassword() == null || !passwordEncoder.matches(rawPassword, u.getHashPassword()))
            throw new DomainException("Credenciales inválidas");

        List<String> roles = usuarioRepo.findRolesByUsuarioId(u.getId());
        String access = jwtService.createAccessToken(u, roles);
        String refresh = jwtService.createRefreshToken(u);

        refreshRepo.save(RefreshToken.builder()
                .usuario(u)
                .tokenHash(HashUtils.sha256(refresh))
                .expiraEn(OffsetDateTime.now().plusDays(props.refreshTtlDays()))
                .revocado(false)
                .ip(ip)
                .userAgent(userAgent)
                .build());

        return new AuthTokensResponse("Bearer", access, props.accessTtlMinutes() * 60L, refresh);
    }

    /** Refuerzo de refresh: firma/exp + iss/aud + typ=refresh + atado a usuario + rotación + pwdChangedAt */
    @Override
    @Transactional
    public AuthTokensResponse refresh(String refreshToken, String ip, String userAgent) {
        // 1) Validación criptográfica y reclamos esperados
        var claims = jwtService.parseAndValidate(refreshToken, "refresh");
        Long userId = Long.parseLong(claims.getSubject());

        // 2) Buscar en BD el refresh del usuario (hash + no revocado)
        String hash = HashUtils.sha256(refreshToken);
        RefreshToken stored = refreshRepo
                .findByUsuario_IdAndTokenHashAndRevocadoFalse(userId, hash)
                .orElseThrow(() -> new DomainException("Refresh inválido"));

        if (Boolean.TRUE.equals(stored.getRevocado()) ||
                stored.getExpiraEn().isBefore(OffsetDateTime.now())) {
            stored.setRevocado(true);
            refreshRepo.save(stored);
            throw new DomainException("Refresh expirado o revocado");
        }

        Usuario u = stored.getUsuario();
        if (Boolean.FALSE.equals(u.getActivo()))
            throw new DomainException("Usuario inactivo");

        // 2.1) Invalida tokens emitidos antes de un cambio de contraseña
        if (u.getPwdChangedAt() != null) {
            Date iat = claims.getIssueTime(); // debe venir en el JWT
            if (iat == null) {
                stored.setRevocado(true);
                refreshRepo.save(stored);
                throw new DomainException("JWT sin iat");
            }
            if (iat.toInstant().isBefore(u.getPwdChangedAt().toInstant())) {
                stored.setRevocado(true);
                refreshRepo.save(stored);
                throw new DomainException("Refresh inválido por cambio de contraseña");
            }
        }

        // 3) Rotación: revoca el usado y emite uno nuevo
        stored.setRevocado(true);
        refreshRepo.save(stored);

        List<String> roles = usuarioRepo.findRolesByUsuarioId(u.getId());
        String access = jwtService.createAccessToken(u, roles);
        String refresh = jwtService.createRefreshToken(u);

        refreshRepo.save(RefreshToken.builder()
                .usuario(u)
                .tokenHash(HashUtils.sha256(refresh))
                .expiraEn(OffsetDateTime.now().plusDays(props.refreshTtlDays()))
                .revocado(false)
                .ip(ip)
                .userAgent(userAgent)
                .build());

        return new AuthTokensResponse("Bearer", access, props.accessTtlMinutes() * 60L, refresh);
    }

    /** Logout atado al usuario y validando JWT de tipo refresh */
    @Override
    @Transactional
    public void logout(String refreshToken) {
        var claims = jwtService.parseAndValidate(refreshToken, "refresh");
        Long userId = Long.parseLong(claims.getSubject());
        String hash = HashUtils.sha256(refreshToken);

        refreshRepo.findByUsuario_IdAndTokenHashAndRevocadoFalse(userId, hash)
                .ifPresent(rt -> {
                    rt.setRevocado(true);
                    refreshRepo.save(rt);
                });
    }

    /** 🔸 Cambio de contraseña: valida, actualiza hash, marca pwdChangedAt y revoca todos los refresh activos */
    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        Usuario u = usuarioRepo.findById(userId)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));

        if (u.getHashPassword() == null || !passwordEncoder.matches(oldPassword, u.getHashPassword())) {
            throw new DomainException("Contraseña actual incorrecta");
        }
        if (oldPassword.equals(newPassword)) {
            throw new DomainException("La nueva contraseña no puede ser igual a la anterior");
        }

        u.setHashPassword(passwordEncoder.encode(newPassword));
        u.setPwdChangedAt(OffsetDateTime.now());
        usuarioRepo.save(u);

        // Revoca todos los refresh activos del usuario
        refreshRepo.revokeAllActiveByUser(userId);
    }
}
