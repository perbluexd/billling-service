// src/main/java/com/cvanguardistas/billing_service/service/mapper/ProgramacionMapper.java
package com.cvanguardistas.billing_service.service.mapper;

import com.cvanguardistas.billing_service.dto.*;
import com.cvanguardistas.billing_service.entities.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface ProgramacionMapper {

    // Calendario
    CalendarioDto toDto(Calendario e);

    @Mapping(target = "id", ignore = true) // evitamos mapear el id en creación
    Calendario toEntity(CrearCalendarioRequest r);

    // Excepción de calendario
    @Mapping(target = "calendarioId", source = "calendario.id")
    CalendarioExcepcionDto toDto(CalendarioExcepcion e);

    // Tarea de programa
    @Mapping(target = "subPresupuestoId", source = "subPresupuesto.id")
    @Mapping(target = "partidaId",
            expression = "java(e.getPartida() != null ? e.getPartida().getId() : null)")
    @Mapping(target = "calendarioId",
            expression = "java(e.getCalendario() != null ? e.getCalendario().getId() : null)")
    TareaProgramaDto toDto(TareaPrograma e);

    // Dependencia entre tareas
    @Mapping(target = "predecesoraId", source = "predecesora.id")
    @Mapping(target = "sucesoraId", source = "sucesora.id")
    TareaDependenciaDto toDto(TareaDependencia e);
}
