package com.cvanguardistas.billing_service.dto;

import jakarta.validation.constraints.NotBlank;

/** Para POST /presupuestos/{id}/SubPresupuestos */
public record CrearSubPresupuestoRequest(
        @NotBlank String nombre
) {}
