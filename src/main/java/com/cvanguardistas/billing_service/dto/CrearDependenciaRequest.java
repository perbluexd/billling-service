// src/main/java/com/cvanguardistas/billing_service/dto/CrearDependenciaRequest.java
package com.cvanguardistas.billing_service.dto;

import com.cvanguardistas.billing_service.entities.TipoDependencia;

import java.math.BigDecimal;

public record CrearDependenciaRequest(
        Long predecesoraId,
        Long sucesoraId,
        TipoDependencia tipo,
        BigDecimal desfase
) {}
