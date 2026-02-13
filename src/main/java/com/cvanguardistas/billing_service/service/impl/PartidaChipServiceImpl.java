// src/main/java/com/cvanguardistas/billing_service/service/impl/PartidaChipServiceImpl.java
package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.entities.*;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.PartidaRepository;
import com.cvanguardistas.billing_service.repository.PartidaTotalCategoriaRepository;
import com.cvanguardistas.billing_service.service.PartidaChipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PartidaChipServiceImpl implements PartidaChipService {

    private final PartidaRepository partidaRepo;
    private final PartidaTotalCategoriaRepository ptcRepo;

    @Override
    @Transactional
    public void setOverride(Long partidaId, Long categoriaId, Boolean usarOverride, BigDecimal unitarioOverride) {
        Partida hoja = partidaRepo.findById(partidaId)
                .orElseThrow(() -> new DomainException("Partida no encontrada: " + partidaId));
        if (hoja.getTipo() != TipoPartida.HOJA) {
            throw new DomainException("El override de chips solo aplica a partidas HOJA");
        }

        PartidaTotalCategoriaId id = new PartidaTotalCategoriaId(partidaId, categoriaId);
        PartidaTotalCategoria chip = ptcRepo.findById(id)
                .orElseThrow(() -> new DomainException("Chip no existente para esa categoría en la hoja"));

        if (usarOverride != null) chip.setUsarOverride(usarOverride);
        // Si activar override sin valor → error explícito
        if (Boolean.TRUE.equals(chip.getUsarOverride()) && unitarioOverride == null) {
            throw new DomainException("unitarioOverride es requerido cuando usarOverride=true");
        }
        chip.setUnitarioOverride(unitarioOverride);

        ptcRepo.save(chip);
    }
}
