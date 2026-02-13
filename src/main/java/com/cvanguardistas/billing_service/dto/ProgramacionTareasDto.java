// src/main/java/com/cvanguardistas/billing_service/dto/ProgramacionTareasDto.java
package com.cvanguardistas.billing_service.dto;

import java.util.List;

public record ProgramacionTareasDto(
        List<TareaProgramaDto> tareas,
        List<TareaDependenciaDto> dependencias
) {}
