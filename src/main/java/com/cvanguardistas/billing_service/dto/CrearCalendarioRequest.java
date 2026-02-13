// src/main/java/com/cvanguardistas/billing_service/dto/CrearCalendarioRequest.java
package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;

public record CrearCalendarioRequest(
        String nombre,
        BigDecimal horasPorDia,
        String zonaHoraria
) {}
