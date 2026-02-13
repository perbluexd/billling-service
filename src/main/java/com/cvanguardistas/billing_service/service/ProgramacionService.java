// src/main/java/com/cvanguardistas/billing_service/service/ProgramacionService.java
package com.cvanguardistas.billing_service.service;

import com.cvanguardistas.billing_service.dto.*;

import java.util.List;

public interface ProgramacionService {

    // Calendarios
    List<CalendarioDto> listarCalendarios();
    CalendarioDto crearCalendario(CrearCalendarioRequest r);
    void eliminarCalendario(Long id);
    List<CalendarioExcepcionDto> listarExcepciones(Long calendarioId);
    CalendarioExcepcionDto agregarExcepcion(UpsertCalendarioExcepcionRequest r);
    void eliminarExcepcion(Long excepcionId);

    // Tareas
    ProgramacionTareasDto listarTareas(Long SubPresupuestoId);
    TareaProgramaDto crearTarea(CrearTareaRequest r);
    TareaProgramaDto actualizarTarea(Long tareaId, UpdateTareaRequest r);
    void eliminarTarea(Long tareaId);

    // Dependencias
    TareaDependenciaDto crearDependencia(CrearDependenciaRequest r);
    void eliminarDependencia(Long dependenciaId);

    // Export
    byte[] exportarCsv(Long SubPresupuestoId);
}
