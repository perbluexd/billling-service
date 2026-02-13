// src/main/java/.../dto/ChangePasswordRequest.java
package com.cvanguardistas.billing_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank @Size(min = 8, max = 120) String newPassword
) {}
