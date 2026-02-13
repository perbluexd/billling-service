package com.cvanguardistas.billing_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LineaACURequest(
        @NotNull Long insumoId,
        @NotNull Long categoriaCostoId,
        @NotNull Boolean dependeDeRendimiento,
        @DecimalMin(value = "0.0", inclusive = false, message = "cuadrillaFrac > 0 si dependeDeRendimiento=true")
        BigDecimal cuadrillaFrac,          // obligatorio si depende=true
        @DecimalMin(value = "0.0", inclusive = false, message = "cantidadFija > 0 si dependeDeRendimiento=false")
        BigDecimal cantidadFija,           // obligatorio si depende=false
        BigDecimal puOverride,             // opcional
        Boolean usarPuOverride             // opcional (default false)
) {}