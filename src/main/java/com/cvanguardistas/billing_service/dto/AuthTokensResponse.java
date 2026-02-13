package com.cvanguardistas.billing_service.dto;

public record AuthTokensResponse(
        String tokenType, // "Bearer"
        String accessToken,
        long   expiresIn, // segundos
        String refreshToken
) {}
