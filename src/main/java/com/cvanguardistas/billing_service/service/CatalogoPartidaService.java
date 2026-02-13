package com.cvanguardistas.billing_service.service;

import com.cvanguardistas.billing_service.dto.*;
import java.util.List;

public interface CatalogoPartidaService {
    Long crear(CrearCatalogoPartidaRequest req);
    void actualizar(Long id, ActualizarCatalogoPartidaRequest req);
    void eliminar(Long id);
    List<CatalogoPartidaListItemDto> listarPorPlantilla(Long plantillaId);
}
