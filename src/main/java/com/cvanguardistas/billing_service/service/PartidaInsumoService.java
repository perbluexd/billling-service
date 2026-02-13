// src/main/java/com/cvanguardistas/billing_service/service/PartidaInsumoService.java
package com.cvanguardistas.billing_service.service;

import com.cvanguardistas.billing_service.dto.HojaDto;
import com.cvanguardistas.billing_service.dto.LineaACUDto;
import com.cvanguardistas.billing_service.dto.LineaACURequest;
import com.cvanguardistas.billing_service.dto.SpInsumoAgregadoDto;

import java.util.List;

public interface PartidaInsumoService {

    /** Lista las líneas ACU de una partida HOJA. */
    List<LineaACUDto> listar(Long partidaId);

    /** Agrega una línea ACU a la partida HOJA. */
    LineaACUDto agregar(Long partidaId, LineaACURequest req);

    /** Edita una línea ACU existente. */
    LineaACUDto editar(Long lineaId, LineaACURequest req);

    /** Elimina una línea ACU. */
    void eliminar(Long lineaId);

    /** Elimina una línea ACU y recalcula la HOJA (retorna la HOJA actualizada). */
    HojaDto eliminarYRecalcular(Long lineaId);

    /** Agregado de insumos por SubPresupuesto. */
    List<SpInsumoAgregadoDto> listarAgregadoPorSubPresupuesto(Long subPresupuestoId);
}
