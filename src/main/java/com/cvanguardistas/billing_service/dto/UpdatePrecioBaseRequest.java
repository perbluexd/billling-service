package com.cvanguardistas.billing_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdatePrecioBaseRequest(
        @NotNull
        @DecimalMin(value = "0.00", inclusive = true, message = "El precio no puede ser negativo")
        @Digits(integer = 14, fraction = 6)
        BigDecimal precio,

        // Si en el futuro quieres registrar histórico, úsalo.
        Boolean registrarHistorial
) {}
