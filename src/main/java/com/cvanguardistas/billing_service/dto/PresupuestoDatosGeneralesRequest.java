package com.cvanguardistas.billing_service.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PresupuestoDatosGeneralesRequest(
        String grupo,                 // nombre del grupo (opcional). Si viene vacío -> quita grupo
        @NotBlank String cliente,
        @NotBlank String direccion,
        @NotBlank String distrito,
        @NotBlank String provincia,
        @NotBlank String departamento,
        @NotNull LocalDate fechaBase,
        @NotNull @Positive BigDecimal jornadaHoras,
        @NotBlank String moneda
) {}
