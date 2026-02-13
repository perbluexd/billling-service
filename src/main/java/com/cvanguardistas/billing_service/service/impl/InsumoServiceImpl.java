package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.dto.*;
import com.cvanguardistas.billing_service.entities.*;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.*;
import com.cvanguardistas.billing_service.service.InsumoService;
import com.cvanguardistas.billing_service.service.mapper.InsumoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class InsumoServiceImpl implements InsumoService {

    private final InsumoRepository insumoRepo;
    private final UnidadRepository unidadRepo;
    private final TipoInsumoRepository tipoInsumoRepo;
    private final PartidaInsumoRepository partidaInsumoRepo;

    private final InsumoMapper insumoMapper;

    // ========= LISTAR (legacy) =========
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InsumoListItemDto> listar(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by("codigo").ascending());
        Page<Insumo> data = insumoRepo.findAll(pageable);

        var content = data.getContent().stream()
                .map(insumoMapper::toListItem)
                .toList();

        return new PagedResponse<>(content, data.getNumber(), data.getSize(), data.getTotalElements(), data.getTotalPages());
    }

    // ========= LISTAR con filtros (Specifications) =========
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InsumoListItemDto> listar(int page, int size, String tipo, String q) {
        var pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 200),
                Sort.by("codigo").ascending()
        );

        String tipoLower = (tipo == null || tipo.isBlank()) ? null : tipo.trim().toLowerCase();
        String qLower    = (q == null || q.isBlank())    ? null : q.trim().toLowerCase();

        // Construcción composable de la Specification
        Specification<Insumo> spec = Specification.allOf(
                com.cvanguardistas.billing_service.repository.specs.InsumoSpecs.tipoCodigoEquals(tipoLower),
                com.cvanguardistas.billing_service.repository.specs.InsumoSpecs.textoContains(qLower)
        );

        // .and(InsumoSpecs.activoEquals(true)) // si deseas forzar solo activos

        Page<Insumo> data = insumoRepo.findAll(spec, pageable); // si spec==null, trae todo

        var content = data.getContent().stream()
                .map(insumoMapper::toListItem)
                .toList();

        return new PagedResponse<>(content, data.getNumber(), data.getSize(), data.getTotalElements(), data.getTotalPages());
    }

    // ========= CREAR =========
    @Override
    @Transactional
    public Long crear(CrearInsumoRequest req) {
        String codigo = req.codigo().trim();
        if (insumoRepo.existsByCodigoIgnoreCase(codigo)) {
            throw new DomainException("Ya existe un insumo con código: " + codigo);
        }

        Unidad unidad = unidadRepo.findByCodigoIgnoreCase(req.unidadCodigo().trim())
                .orElseThrow(() -> new DomainException("Unidad no encontrada: " + req.unidadCodigo()));

        TipoInsumo tipo = tipoInsumoRepo.findByCodigoIgnoreCase(req.tipoInsumoCodigo().trim())
                .orElseThrow(() -> new DomainException("Tipo de insumo no encontrado: " + req.tipoInsumoCodigo()));

        Insumo i = Insumo.builder()
                .codigo(codigo)
                .nombre(req.nombre().trim())
                .unidad(unidad)
                .tipoInsumo(tipo)
                .precioBase(req.precioBase())
                .colorHex(req.colorHex())
                .activo(req.activo() == null ? Boolean.TRUE : req.activo())
                .build();

        i = insumoRepo.save(i);
        return i.getId();
    }

    // ========= ACTUALIZAR (patch) =========
    @Override
    @Transactional
    public void actualizar(Long id, ActualizarInsumoRequest req) {
        Insumo i = insumoRepo.findById(id)
                .orElseThrow(() -> new DomainException("Insumo no encontrado: " + id));

        if (req.nombre() != null) i.setNombre(req.nombre().trim());

        if (req.unidadCodigo() != null) {
            Unidad u = unidadRepo.findByCodigoIgnoreCase(req.unidadCodigo().trim())
                    .orElseThrow(() -> new DomainException("Unidad no encontrada: " + req.unidadCodigo()));
            i.setUnidad(u);
        }

        if (req.tipoInsumoCodigo() != null) {
            TipoInsumo t = tipoInsumoRepo.findByCodigoIgnoreCase(req.tipoInsumoCodigo().trim())
                    .orElseThrow(() -> new DomainException("Tipo de insumo no encontrado: " + req.tipoInsumoCodigo()));
            i.setTipoInsumo(t);
        }

        if (req.precioBase() != null) {
            if (req.precioBase().signum() < 0) throw new DomainException("Precio inválido");
            i.setPrecioBase(req.precioBase());
        }

        if (req.colorHex() != null) i.setColorHex(req.colorHex());
        if (req.activo() != null)   i.setActivo(req.activo());

        insumoRepo.save(i);
    }

    // ========= ELIMINAR =========
    @Override
    @Transactional
    public void eliminar(Long id) {
        Insumo i = insumoRepo.findById(id)
                .orElseThrow(() -> new DomainException("Insumo no encontrado: " + id));

        if (partidaInsumoRepo.existsByInsumo_Id(id)) {
            throw new DomainException("No se puede eliminar: el insumo está siendo usado en partidas.");
        }

        insumoRepo.delete(i);
    }

    // ========= ACTUALIZAR PRECIO BASE =========
    @Override
    @Transactional
    public InsumoListItemDto actualizarPrecioBase(Long insumoId, BigDecimal nuevoPrecio, boolean registrarHistorial) {
        if (nuevoPrecio == null || nuevoPrecio.signum() < 0) {
            throw new DomainException("Precio inválido");
        }

        Insumo insumo = insumoRepo.findById(insumoId)
                .orElseThrow(() -> new DomainException("Insumo no encontrado: " + insumoId));

        insumo.setPrecioBase(nuevoPrecio);
        insumo = insumoRepo.save(insumo);

        // (Opcional) Registrar histórico de precios
        // if (registrarHistorial) {
        //     var hist = new InsumoPrecioHist();
        //     hist.setInsumo(insumo);
        //     hist.setPrecio(nuevoPrecio);
        //     hist.setVigenteDesde(LocalDate.now());
        //     hist.setFuente(FuentePrecio.MANUAL);
        //     histRepo.save(hist);
        // }

        return insumoMapper.toListItem(insumo);
    }
}
