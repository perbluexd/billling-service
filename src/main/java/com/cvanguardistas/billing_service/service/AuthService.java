package com.cvanguardistas.billing_service.service;

import com.cvanguardistas.billing_service.dto.AuthTokensResponse;

public interface AuthService {
    AuthTokensResponse login(String email, String rawPassword, String ip, String userAgent);
    AuthTokensResponse refresh(String refreshToken, String ip, String userAgent);
    void logout(String refreshToken);

    // 🔸 Nuevo
    void changePassword(Long userId, String oldPassword, String newPassword);
}
