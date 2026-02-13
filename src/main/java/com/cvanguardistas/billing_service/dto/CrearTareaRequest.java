// src/main/java/com/cvanguardistas/billing_service/dto/CrearTareaRequest.java
package com.cvanguardistas.billing_service.dto;

import com.cvanguardistas.billing_service.entities.TipoTareaPrograma;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CrearTareaRequest(
        Long subPresupuestoId,
        Long partidaId,
        String nombre,
        TipoTareaPrograma tipo,
        BigDecimal duracionDias,
        LocalDate fechaInicio,
        Long calendarioId,
        Integer orden
) {}
