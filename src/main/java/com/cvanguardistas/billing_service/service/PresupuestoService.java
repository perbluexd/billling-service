package com.cvanguardistas.billing_service.service;

import com.cvanguardistas.billing_service.dto.PagedResponse;
import com.cvanguardistas.billing_service.dto.PresupuestoDetalleDto;
import com.cvanguardistas.billing_service.dto.PresupuestoListItemDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PresupuestoService {

    // ======================
    // EXISTENTES
    // ======================

    Long crearEnBlanco(String nombre, LocalDate fechaBase, String moneda, BigDecimal jornadaHoras);

    void aprobar(Long presupuestoId, String version);

    /**
     * Listado paginado legacy (se mantiene por compatibilidad).
     */
    PagedResponse<PresupuestoListItemDto> listar(int page, int size);

    PresupuestoDetalleDto detalle(Long id);

    void renombrar(Long id, String nuevoNombre);

    void eliminar(Long id);

    // ======================
    // NUEVOS
    // ======================

    /**
     * Listado paginado con filtros opcionales.
     * @param page índice de página (0-based)
     * @param size tamaño de página
     * @param grupo nombre exacto del grupo (case-insensitive). Si es null, trae todos.
     * @param q     texto libre en nombre o cliente (case-insensitive). Si es null, sin filtro.
     */
    PagedResponse<PresupuestoListItemDto> listar(int page, int size, String grupo, String q);

    /**
     * Actualiza datos generales del presupuesto en una sola operación.
     * Cualquier parámetro puede venir null para "no modificar" ese campo.
     */
    void actualizarDatosGenerales(Long id,
                                  String grupoNombre,
                                  String cliente,
                                  String direccion,
                                  String distrito,
                                  String provincia,
                                  String departamento,
                                  LocalDate fechaBase,
                                  BigDecimal jornadaHoras,
                                  String moneda);
}
