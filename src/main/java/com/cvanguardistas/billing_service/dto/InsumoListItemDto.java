package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InsumoListItemDto(
        Long id,
        String codigo,
        String nombre,
        Long unidadId,
        String unidadCodigo,
        BigDecimal precioBase,
        LocalDateTime actualizadoEn
) {}
