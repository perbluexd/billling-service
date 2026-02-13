// src/main/java/com/cvanguardistas/billing_service/dto/UpdateTareaRequest.java
package com.cvanguardistas.billing_service.dto;

import com.cvanguardistas.billing_service.entities.TipoTareaPrograma;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateTareaRequest(
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
