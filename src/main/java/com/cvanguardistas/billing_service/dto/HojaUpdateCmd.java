package com.cvanguardistas.billing_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Comando de actualización de HOJA.
 * Todos los campos (excepto partidaId y lineas!=null) son opcionales:
 * - Si vienen null, no se cambian.
 * - lineas puede venir vacía si solo cambias metrado/rendimiento/unidad/padre/orden.
 */
public record HojaUpdateCmd(
        Long partidaId,
        @DecimalMin("0.0") BigDecimal metrado,
        @DecimalMin(value = "0.000001") BigDecimal rendimiento,
        Long unidadId,
        Long padreId,              // opcional: mover hoja dentro del árbol
        Integer orden,             // opcional: nuevo orden relativo
        @NotNull @Size(min = 0) List<LineaACURequest> lineas
) { }
