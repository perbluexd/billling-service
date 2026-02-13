package com.cvanguardistas.billing_service.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CrearInsumoRequest(
        @NotBlank String codigo,
        @NotBlank String nombre,

        // claves naturales
        @NotBlank String unidadCodigo,          // p.ej. "HH", "KG", "M3", "UND"
        @NotBlank String tipoInsumoCodigo,      // p.ej. "MO", "MT", "EQ", "SC"

        @DecimalMin(value = "0.00", inclusive = true, message = "El precio no puede ser negativo")
        @Digits(integer = 14, fraction = 6)
        BigDecimal precioBase,

        String colorHex,

        Boolean activo
) {}
