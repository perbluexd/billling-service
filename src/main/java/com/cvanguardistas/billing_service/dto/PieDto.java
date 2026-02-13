// src/main/java/com/cvanguardistas/billing_service/dto/PieDto.java
package com.cvanguardistas.billing_service.dto;

import java.util.List;

public record PieDto(
        List<SPFormulaDto> formulas
) {}
