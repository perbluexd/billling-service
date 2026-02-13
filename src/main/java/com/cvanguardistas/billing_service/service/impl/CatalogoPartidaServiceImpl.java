package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.dto.*;
import com.cvanguardistas.billing_service.entities.*;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.*;
import com.cvanguardistas.billing_service.service.CatalogoPartidaService;
import com.cvanguardistas.billing_service.service.mapper.CatalogoPartidaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoPartidaServiceImpl implements CatalogoPartidaService {

    private final PlantillaRepository plantillaRepo;
    private final UnidadRepository unidadRepo;
    private final PlantillaPartidaRepository catalogoRepo;
    private final CatalogoPartidaMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoPartidaListItemDto> listarPorPlantilla(Long plantillaId) {
        var list = catalogoRepo.findByPlantilla_IdOrderByOrdenAsc(plantillaId);
        return list.stream().map(mapper::toListItem).toList();
    }

    @Override
    @Transactional
    public Long crear(CrearCatalogoPartidaRequest req) {
        var plantilla = plantillaRepo.findById(req.plantillaId())
                .orElseThrow(() -> new DomainException("Plantilla no encontrada: " + req.plantillaId()));

        PlantillaPartida padre = null;
        if (req.padreId() != null) {
            padre = catalogoRepo.findById(req.padreId())
                    .orElseThrow(() -> new DomainException("Padre no encontrado: " + req.padreId()));
        }

        Unidad unidad = null;
        if (req.unidadId() != null) {
            unidad = unidadRepo.findById(req.unidadId())
                    .orElseThrow(() -> new DomainException("Unidad no encontrada: " + req.unidadId()));
        }

        PlantillaPartida p = PlantillaPartida.builder()
                .plantilla(plantilla)
                .padre(padre)
                .codigo(req.codigo())
                .nombre(req.nombre())
                .unidad(unidad)
                .cantidadBase(req.cantidadBase())
                .rendimientoBase(req.rendimientoBase())
                .precioUnitRef(req.precioUnitRef())
                .orden(req.orden())
                .build();

        p = catalogoRepo.save(p);
        return p.getId();
    }

    @Override
    @Transactional
    public void actualizar(Long id, ActualizarCatalogoPartidaRequest req) {
        PlantillaPartida p = catalogoRepo.findById(id)
                .orElseThrow(() -> new DomainException("Catálogo no encontrado: " + id));

        if (req.nombre() != null) p.setNombre(req.nombre());
        if (req.unidadId() != null)
            p.setUnidad(unidadRepo.findById(req.unidadId())
                    .orElseThrow(() -> new DomainException("Unidad no encontrada: " + req.unidadId())));
        if (req.cantidadBase() != null) p.setCantidadBase(req.cantidadBase());
        if (req.rendimientoBase() != null) p.setRendimientoBase(req.rendimientoBase());
        if (req.precioUnitRef() != null) p.setPrecioUnitRef(req.precioUnitRef());
        if (req.orden() != null) p.setOrden(req.orden());

        catalogoRepo.save(p);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!catalogoRepo.existsById(id))
            throw new DomainException("Catálogo no encontrado: " + id);
        catalogoRepo.deleteById(id);
    }
}
