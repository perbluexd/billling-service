// src/main/java/com/cvanguardistas/billing_service/dto/CrearGGItemDetalleRequest.java
package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;

public record CrearGGItemDetalleRequest(
        Long ggItemId,
        String descripcion,
        String unidad,
        String cantidadDesc,
        BigDecimal cantidad,
        BigDecimal precio,
        BigDecimal porcentaje,
        Integer orden
) {}
