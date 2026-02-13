// src/main/java/com/cvanguardistas/billing_service/dto/CrearGGItemRequest.java
package com.cvanguardistas.billing_service.dto;

import com.cvanguardistas.billing_service.entities.FormatoGG;
import com.cvanguardistas.billing_service.entities.TipoGG;

public record CrearGGItemRequest(
        Long subPresupuestoId,
        TipoGG tipo,
        FormatoGG formato,
        String titulo,
        Integer orden
) {}
