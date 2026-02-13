// src/main/java/com/cvanguardistas/billing_service/dto/CalendarioDto.java
package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;

public record CalendarioDto(
        Long id,
        String nombre,
        BigDecimal horasPorDia,
        String zonaHoraria
) {}
