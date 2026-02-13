package com.cvanguardistas.billing_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CrearCatalogoPartidaRequest(
        @NotNull Long plantillaId,
        Long padreId,
        @NotBlank String codigo,
        @NotBlank String nombre,
        Long unidadId,
        BigDecimal cantidadBase,
        BigDecimal rendimientoBase,
        BigDecimal precioUnitRef,
        Integer orden
) {}
