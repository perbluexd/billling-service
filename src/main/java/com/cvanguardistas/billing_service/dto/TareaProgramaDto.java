// src/main/java/com/cvanguardistas/billing_service/dto/TareaProgramaDto.java
package com.cvanguardistas.billing_service.dto;

import com.cvanguardistas.billing_service.entities.TipoTareaPrograma;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TareaProgramaDto(
        Long id,
        Long subPresupuestoId,
        Long partidaId,
        String nombre,
        TipoTareaPrograma tipo,
        BigDecimal duracionDias,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal porcentajeAvance,
        Long calendarioId,
        Integer orden,
        Boolean esRutaCritica
) {}
