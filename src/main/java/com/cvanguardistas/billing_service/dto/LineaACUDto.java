package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;

public record LineaACUDto(
        Long partidaInsumoId,   // puede ser null si aún no está persistido
        Long insumoId,
        Long categoriaCostoId,
        BigDecimal cantidad,
        BigDecimal pu,
        BigDecimal parcial
) {}