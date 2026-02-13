package com.cvanguardistas.billing_service.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRefreshRequest(@NotBlank String refreshToken) {}
