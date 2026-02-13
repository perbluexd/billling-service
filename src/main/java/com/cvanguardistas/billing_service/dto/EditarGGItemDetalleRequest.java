// src/main/java/com/cvanguardistas/billing_service/dto/EditarGGItemDetalleRequest.java
package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;

public record EditarGGItemDetalleRequest(
        String descripcion,
        String unidad,
        String cantidadDesc,
        BigDecimal cantidad,
        BigDecimal precio,
        BigDecimal porcentaje,
        Integer orden
) {}
