package com.cvanguardistas.billing_service.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

/** Patch semantics: todos los campos opcionales; si vienen null, no cambian. */
public record ActualizarInsumoRequest(
        String nombre,
        String unidadCodigo,
        String tipoInsumoCodigo,

        @DecimalMin(value = "0.00", inclusive = true, message = "El precio no puede ser negativo")
        @Digits(integer = 14, fraction = 6)
        BigDecimal precioBase,

        String colorHex,
        Boolean activo
) {}
