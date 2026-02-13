// src/main/java/com/cvanguardistas/billing_service/dto/GGItemDto.java
package com.cvanguardistas.billing_service.dto;

import com.cvanguardistas.billing_service.entities.FormatoGG;
import com.cvanguardistas.billing_service.entities.TipoGG;

public record GGItemDto(
        Long id,
        Long subPresupuestoId,
        TipoGG tipo,
        FormatoGG formato,
        String titulo,
        Integer orden
) {}
