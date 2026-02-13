// src/main/java/com/cvanguardistas/billing_service/dto/CalendarioExcepcionDto.java
package com.cvanguardistas.billing_service.dto;

import com.cvanguardistas.billing_service.entities.CalendarioExcepcionTipo;

import java.time.LocalDate;

public record CalendarioExcepcionDto(
        Long id,
        Long calendarioId,
        LocalDate fecha,
        CalendarioExcepcionTipo tipo,
        String descripcion
) {}
