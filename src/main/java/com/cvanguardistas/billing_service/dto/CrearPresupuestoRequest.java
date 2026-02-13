package com.cvanguardistas.billing_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Para POST /presupuestos */
public record CrearPresupuestoRequest(
        @NotBlank String nombre,
        @NotNull LocalDate fechaBase,
        @NotBlank String moneda,
        @NotNull BigDecimal jornadaHoras
) {}
