// src/main/java/com/cvanguardistas/billing_service/service/impl/PartidaInsumoServiceImpl.java
package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.dto.HojaDto;
import com.cvanguardistas.billing_service.dto.HojaUpdateCmd;               // <-- DTO correcto
import com.cvanguardistas.billing_service.dto.LineaACUDto;
import com.cvanguardistas.billing_service.dto.LineaACURequest;
import com.cvanguardistas.billing_service.dto.SpInsumoAgregadoDto;
import com.cvanguardistas.billing_service.entities.Partida;
import com.cvanguardistas.billing_service.entities.PartidaInsumo;
import com.cvanguardistas.billing_service.entities.TipoPartida;
import com.cvanguardistas.billing_service.entities.AccionAuditoria;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.CategoriaCostoRepository;
import com.cvanguardistas.billing_service.repository.InsumoRepository;
import com.cvanguardistas.billing_service.repository.PartidaInsumoRepository;
import com.cvanguardistas.billing_service.repository.PartidaRepository;
import com.cvanguardistas.billing_service.service.PartidaInsumoService;
import com.cvanguardistas.billing_service.service.PartidaService;          // <-- inyectado
import com.cvanguardistas.billing_service.service.mapper.PartidaMapper;
import com.cvanguardistas.billing_service.web.audit.Auditable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Collator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PartidaInsumoServiceImpl implements PartidaInsumoService {

    private final PartidaInsumoRepository repo;
    private final PartidaRepository partidaRepo;
    private final InsumoRepository insumoRepo;
    private final CategoriaCostoRepository categoriaRepo;
    private final PartidaMapper mapper;
    private final PartidaService partidaService; // <-- NUEVO

    @Override
    @Transactional(readOnly = true)
    public List<LineaACUDto> listar(Long partidaId) {
        return repo.findByPartidaId(partidaId).stream()
                .map(mapper::toLineaACUDto)
                .toList();
    }

    @Override
    @Transactional
    @Auditable(entidad = "PartidaInsumo", accion = AccionAuditoria.CREAR, entityIdFromResult = "#result.id")
    public LineaACUDto agregar(Long partidaId, LineaACURequest req) {
        Partida hoja = partidaRepo.findById(partidaId)
                .orElseThrow(() -> new DomainException("Partida no encontrada: " + partidaId));
        if (hoja.getTipo() != TipoPartida.HOJA) {
            throw new DomainException("Solo se pueden agregar líneas a hojas");
        }

        PartidaInsumo e = new PartidaInsumo();
        e.setPartida(hoja);
        e.setInsumo(insumoRepo.getReferenceById(req.insumoId()));
        e.setCategoriaCosto(categoriaRepo.getReferenceById(req.categoriaCostoId()));
        e.setDependeDeRendimiento(Boolean.TRUE.equals(req.dependeDeRendimiento()));
        e.setCuadrillaFrac(req.cuadrillaFrac());
        e.setCantidadFija(req.cantidadFija());
        e.setPuOverride(req.puOverride());
        e.setUsarPuOverride(Boolean.TRUE.equals(req.usarPuOverride()));

        return mapper.toLineaACUDto(repo.save(e));
    }

    @Override
    @Transactional
    @Auditable(entidad = "PartidaInsumo", accion = AccionAuditoria.EDITAR, entityId = "#lineaId")
    public LineaACUDto editar(Long lineaId, LineaACURequest req) {
        PartidaInsumo e = repo.findById(lineaId)
                .orElseThrow(() -> new DomainException("Línea no encontrada: " + lineaId));
        e.setInsumo(insumoRepo.getReferenceById(req.insumoId()));
        e.setCategoriaCosto(categoriaRepo.getReferenceById(req.categoriaCostoId()));
        e.setDependeDeRendimiento(Boolean.TRUE.equals(req.dependeDeRendimiento()));
        e.setCuadrillaFrac(req.cuadrillaFrac());
        e.setCantidadFija(req.cantidadFija());
        e.setPuOverride(req.puOverride());
        e.setUsarPuOverride(Boolean.TRUE.equals(req.usarPuOverride()));
        return mapper.toLineaACUDto(repo.save(e));
    }

    @Override
    @Transactional
    @Auditable(entidad = "PartidaInsumo", accion = AccionAuditoria.ELIMINAR, entityId = "#lineaId")
    public void eliminar(Long lineaId) {
        if (!repo.existsById(lineaId)) throw new DomainException("Línea no existe: " + lineaId);
        repo.deleteById(lineaId);
    }

    // NUEVO: eliminar y recalcular la HOJA completa, devolviendo la HOJA actualizada
    @Override
    @Transactional
    @Auditable(entidad = "PartidaInsumo", accion = AccionAuditoria.ELIMINAR, entityId = "#lineaId")
    public HojaDto eliminarYRecalcular(Long lineaId) {
        PartidaInsumo e = repo.findById(lineaId)
                .orElseThrow(() -> new DomainException("Línea no existe: " + lineaId));
        Long partidaId = e.getPartida().getId();

        repo.deleteById(lineaId);

        // Usar el constructor del record HojaUpdateCmd
        HojaUpdateCmd cmd = new HojaUpdateCmd(
                partidaId,
                null,               // metrado
                null,               // rendimiento
                null,               // unidadId
                null,               // padreId
                null,               // orden
                List.of()           // lineas: NOT NULL y puede ser vacío
        );

        return partidaService.actualizarHoja(cmd);
    }

    /**
     * Agregación de insumos a nivel de SubPresupuesto.
     * Implementación en memoria (no requiere query JPA específica).
     */
    @Override
    @Transactional(readOnly = true)
    public List<SpInsumoAgregadoDto> listarAgregadoPorSubPresupuesto(Long subPresupuestoId) {
        if (subPresupuestoId == null || subPresupuestoId < 1) {
            throw new DomainException("SubPresupuestoId inválido: " + subPresupuestoId);
        }

        // Traemos todas las líneas del SP (lazy OK por @Transactional)
        var lineas = repo.findByPartida_SubPresupuesto_Id(subPresupuestoId);

        // Agrupamos por insumo preservando orden de inserción
        Map<Long, SpInsumoAgregadoDto> acc = new LinkedHashMap<>();

        for (PartidaInsumo e : lineas) {
            var insumo = e.getInsumo();
            if (insumo == null) continue; // robustez

            Long key = insumo.getId();
            var existente = acc.get(key);

            BigDecimal cantidad = e.getCantidad() != null ? e.getCantidad() : BigDecimal.ZERO;
            BigDecimal parcial  = e.getParcial()  != null ? e.getParcial()  : BigDecimal.ZERO;

            if (existente == null) {
                acc.put(key, new SpInsumoAgregadoDto(
                        insumo.getId(),
                        insumo.getCodigo(),
                        insumo.getNombre(),
                        insumo.getUnidad() != null ? insumo.getUnidad().getCodigo() : null,
                        cantidad,
                        parcial
                ));
            } else {
                acc.put(key, new SpInsumoAgregadoDto(
                        existente.insumoId(),
                        existente.codigoInsumo(),
                        existente.nombreInsumo(),
                        existente.unidadCodigo(),
                        existente.cantidadTotal().add(cantidad),
                        existente.costoTotal().add(parcial)
                ));
            }
        }

        // Orden sugerido: por nombre de insumo (ajusta si prefieres otro criterio)
        Collator collatorEs = Collator.getInstance(new Locale("es"));
        Comparator<SpInsumoAgregadoDto> porNombre =
                Comparator.comparing(SpInsumoAgregadoDto::nombreInsumo, collatorEs);

        return acc.values().stream()
                .sorted(porNombre)
                .toList();
    }
}
