package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;

public record SpInsumoAgregadoDto(
        Long insumoId,
        String codigoInsumo,
        String nombreInsumo,
        String unidadCodigo,
        BigDecimal cantidadTotal,
        BigDecimal costoTotal
) {}
