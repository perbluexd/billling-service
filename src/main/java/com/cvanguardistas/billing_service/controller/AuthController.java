package com.cvanguardistas.billing_service.controller;

import com.cvanguardistas.billing_service.dto.AuthLoginRequest;
import com.cvanguardistas.billing_service.dto.AuthRefreshRequest;
import com.cvanguardistas.billing_service.dto.AuthTokensResponse;
import com.cvanguardistas.billing_service.dto.ChangePasswordRequest;
import com.cvanguardistas.billing_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService; // <-- renombrado para evitar colisión con el parámetro

    @PostMapping("/login")
    public ResponseEntity<AuthTokensResponse> login(@RequestBody @Valid AuthLoginRequest req,
                                                    HttpServletRequest http) {
        String ua = http.getHeader("User-Agent");
        String ip = http.getRemoteAddr();
        return ResponseEntity.ok(authService.login(req.email(), req.password(), ip, ua));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokensResponse> refresh(@RequestBody @Valid AuthRefreshRequest req,
                                                      HttpServletRequest http) {
        String ua = http.getHeader("User-Agent");
        String ip = http.getRemoteAddr();
        return ResponseEntity.ok(authService.refresh(req.refreshToken(), ip, ua));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid AuthRefreshRequest req) {
        authService.logout(req.refreshToken());
        return ResponseEntity.noContent().build();
    }

    /**
     * Cambio de contraseña usando el Sub del JWT como userId.
     * Requiere que el endpoint esté protegido (el SecurityFilterChain debe exigir autenticación).
     */
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest req,
                                               @AuthenticationPrincipal Jwt jwt) {
        // En tu JwtService, el Subject (Sub) es el id del usuario (String.valueOf(u.getId()))
        Long userId = Long.parseLong(jwt.getSubject());
        authService.changePassword(userId, req.oldPassword(), req.newPassword());
        return ResponseEntity.noContent().build();
    }
}
