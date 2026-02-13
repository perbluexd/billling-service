// src/main/java/com/cvanguardistas/billing_service/dto/GGItemDetalleDto.java
package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;

public record GGItemDetalleDto(
        Long id,
        Long ggItemId,
        String descripcion,
        String unidad,
        String cantidadDesc,
        BigDecimal cantidad,
        BigDecimal precio,
        BigDecimal porcentaje,
        BigDecimal parcial,
        Integer orden
) {}
