// com.cvanguardistas.billing_service.service.CatalogoService
package com.cvanguardistas.billing_service.service;

import com.cvanguardistas.billing_service.dto.CatalogoPartidaDto;
import com.cvanguardistas.billing_service.entities.TipoPartida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CatalogoService {
    Page<CatalogoPartidaDto> buscarPartidas(String q, TipoPartida tipo, Pageable pageable);
}
