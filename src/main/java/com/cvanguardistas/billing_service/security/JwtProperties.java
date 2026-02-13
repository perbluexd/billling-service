package com.cvanguardistas.billing_service.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String alg,
        String issuer,
        String audience,
        long accessTtlMinutes,
        long refreshTtlDays,
        String privateKeyPem,
        String publicKeyPem,
        String privateKeyPath,
        String publicKeyPath
) {}
