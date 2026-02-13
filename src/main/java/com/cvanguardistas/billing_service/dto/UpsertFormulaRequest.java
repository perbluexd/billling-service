// src/main/java/com/cvanguardistas/billing_service/dto/UpsertFormulaRequest.java
package com.cvanguardistas.billing_service.dto;

public record UpsertFormulaRequest(
        String expresion,
        String descripcion,
        Boolean resaltar,
        Integer orden
) {}
