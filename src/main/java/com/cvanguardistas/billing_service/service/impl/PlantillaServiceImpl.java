// com.cvanguardistas.billing_service.service.impl.PlantillaServiceImpl
package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.dto.PlantillaResumenDto;
import com.cvanguardistas.billing_service.entities.*;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.*;
import com.cvanguardistas.billing_service.service.PlantillaService;
import com.cvanguardistas.billing_service.service.mapper.PlantillaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service @RequiredArgsConstructor
public class PlantillaServiceImpl implements PlantillaService {

    private final PlantillaRepository plantillaRepo;
    private final SubPresupuestoRepository SubPresRepo;
    private final PresupuestoRepository presupuestoRepo;
    private final PartidaRepository partidaRepo;
    private final PlantillaMapper plantillaMapper;

    @Override
    public List<PlantillaResumenDto> listar() {
        return plantillaRepo.findAll().stream().map(plantillaMapper::toResumen).toList();
    }

    /**
     * Crea un SP y cuelga el árbol (títulos/hojas) según la plantilla.
     * Versión mínima: crea solo un SP “desde plantilla” y una HOJA de ejemplo.
     */
    @Override
    @Transactional
    public Long instanciar(Long plantillaId, Long presupuestoId) {
        var plantilla = plantillaRepo.findById(plantillaId)
                .orElseThrow(() -> new DomainException("Plantilla no encontrada: " + plantillaId));
        var ppto = presupuestoRepo.findById(presupuestoId)
                .orElseThrow(() -> new DomainException("Presupuesto no encontrado: " + presupuestoId));

        // 1) SP nuevo
        SubPresupuesto sp = new SubPresupuesto();
        sp.setPresupuesto(ppto);
        sp.setNombre("Desde plantilla: " + plantilla.getNombre());
        sp.setOrden( (int) (SubPresRepo.count() + 1) );
        sp = SubPresRepo.save(sp);

        // 2) Árbol mínimo: un TÍTULO y una HOJA (ejemplo)
        Partida titulo = new Partida();
        titulo.setSubPresupuesto(sp);
        titulo.setTipo(TipoPartida.TITULO);
        titulo.setCodigo("PT-001");
        titulo.setNombre("Capítulo 1");
        titulo.setOrden(1);
        titulo.setParcial(BigDecimal.ZERO);
        titulo = partidaRepo.save(titulo);

        Partida hoja = new Partida();
        hoja.setSubPresupuesto(sp);
        hoja.setPadre(titulo);
        hoja.setTipo(TipoPartida.HOJA);
        hoja.setCodigo("PH-001");
        hoja.setNombre("Partida base");
        hoja.setUnidad(null);                 // se puede setear luego
        hoja.setRendimiento(new BigDecimal("1"));
        hoja.setMetrado(new BigDecimal("1"));
        hoja.setCu(BigDecimal.ZERO);
        hoja.setParcial(BigDecimal.ZERO);
        hoja.setOrden(1);
        partidaRepo.save(hoja);

        return sp.getId();
    }
}
