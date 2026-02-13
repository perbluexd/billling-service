package com.cvanguardistas.billing_service.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PrecioVigenteQuery(
        @NotNull Long insumoId,
        @NotNull LocalDate fechaBase
) {}