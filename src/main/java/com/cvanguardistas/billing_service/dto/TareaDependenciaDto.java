// src/main/java/com/cvanguardistas/billing_service/dto/TareaDependenciaDto.java
package com.cvanguardistas.billing_service.dto;

import com.cvanguardistas.billing_service.entities.TipoDependencia;

import java.math.BigDecimal;

public record TareaDependenciaDto(
        Long id,
        Long predecesoraId,
        Long sucesoraId,
        TipoDependencia tipo,
        BigDecimal desfase
) {}
