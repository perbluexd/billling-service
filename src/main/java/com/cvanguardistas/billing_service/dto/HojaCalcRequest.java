package com.cvanguardistas.billing_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record HojaCalcRequest(
        @NotNull @DecimalMin("0.0") BigDecimal jornadaEfectiva, // COALESCE(jornada_override, presupuesto.jornada, 8)
        @NotNull @DecimalMin(value = "0.000001") BigDecimal rendimiento,
        @NotNull @DecimalMin("0.0") BigDecimal metrado,
        @NotNull @Size(min = 1) List<LineaACURequest> lineas
) {}