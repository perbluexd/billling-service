// com.cvanguardistas.billing_service.service.impl.CatalogoServiceImpl
package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.dto.CatalogoPartidaDto;
import com.cvanguardistas.billing_service.entities.TipoPartida;
import com.cvanguardistas.billing_service.repository.CatalogoPartidaRepository;
import com.cvanguardistas.billing_service.service.CatalogoService;
import com.cvanguardistas.billing_service.service.mapper.CatalogoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class CatalogoServiceImpl implements CatalogoService {
    private final CatalogoPartidaRepository repo;
    private final CatalogoMapper mapper;

    @Override
    public Page<CatalogoPartidaDto> buscarPartidas(String q, TipoPartida tipo, Pageable pageable) {
        var page = repo.buscar(q, tipo, pageable);
        return page.map(mapper::toDto);
    }
}
