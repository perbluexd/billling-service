package com.cvanguardistas.billing_service.service;

import com.cvanguardistas.billing_service.dto.ActualizarInsumoRequest;
import com.cvanguardistas.billing_service.dto.CrearInsumoRequest;
import com.cvanguardistas.billing_service.dto.InsumoListItemDto;
import com.cvanguardistas.billing_service.dto.PagedResponse;

import java.math.BigDecimal;

public interface InsumoService {

    // EXISTENTES
    PagedResponse<InsumoListItemDto> listar(int page, int size);
    InsumoListItemDto actualizarPrecioBase(Long insumoId, BigDecimal nuevoPrecio, boolean registrarHistorial);

    // NUEVOS
    PagedResponse<InsumoListItemDto> listar(int page, int size, String tipo, String q);
    Long crear(CrearInsumoRequest req);
    void actualizar(Long id, ActualizarInsumoRequest req);
    void eliminar(Long id);
}
