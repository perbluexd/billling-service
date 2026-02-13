package com.cvanguardistas.billing_service.service;

import com.cvanguardistas.billing_service.dto.HojaDto;
import com.cvanguardistas.billing_service.dto.HojaUpdateCmd;
import com.cvanguardistas.billing_service.dto.PartidaArbolDto;
import com.cvanguardistas.billing_service.entities.Partida;
import com.cvanguardistas.billing_service.entities.TipoPartida;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface PartidaService {

    /**
     * Crea una partida (TÍTULO/SubTÍTULO/HOJA).
     * Si tipo = HOJA, unidadId, rendimiento y metrado NO deben ser nulos.
     *
     * @return id de la partida creada
     */
    Long crear(Long subPresupuestoId,
               Long padreId,                 // null si es TÍTULO raíz
               TipoPartida tipo,
               String codigo,
               String nombre,
               Long unidadId,                // requerido si tipo=HOJA
               BigDecimal rendimiento,       // requerido si tipo=HOJA
               BigDecimal metrado,           // requerido si tipo=HOJA
               Integer orden);               // null -> lo calcula al final

    /** Actualiza hoja y dispara recálculo + propagación. */
    @Transactional
    HojaDto actualizarHoja(HojaUpdateCmd cmd);

    /** Devuelve la hoja (DTO) lista para la UI. */
    HojaDto obtenerHoja(Long partidaId);

    /** Árbol simple por SubPresupuesto (puedes mapear a DTO en el controller). */
    List<PartidaArbolDto> obtenerArbol(Long subPresupuestoId);
    Long instanciarDesdeCatalogo(Long subPresupuestoId, Long catalogoPartidaId);
    void eliminar(Long partidaId);
    void moverPartida(Long partidaId, Long nuevoPadreId, Integer nuevoOrden); // si no está aún en la interfaz


}
