package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;
import java.util.List;

public record HojaDto(
        Long partidaId,
        Long unidadId,
        BigDecimal rendimiento,
        BigDecimal metrado,
        BigDecimal cu,
        BigDecimal parcial,
        List<ChipDto> chips,
        List<LineaACUDto> lineas
) {}