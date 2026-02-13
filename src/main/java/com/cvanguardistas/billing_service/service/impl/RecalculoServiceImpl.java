package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.dto.HojaUpdateCmd;
import com.cvanguardistas.billing_service.dto.RecalculoResultDto;
import com.cvanguardistas.billing_service.entities.Partida;
import com.cvanguardistas.billing_service.entities.SubPresupuesto;
import com.cvanguardistas.billing_service.entities.TipoPartida;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.PartidaRepository;
import com.cvanguardistas.billing_service.repository.SubPresupuestoRepository;
import com.cvanguardistas.billing_service.service.GGItemService;
import com.cvanguardistas.billing_service.service.PartidaService;
import com.cvanguardistas.billing_service.service.RecalculoService;
import com.cvanguardistas.billing_service.service.SPFormulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecalculoServiceImpl implements RecalculoService {

    private final PartidaRepository partidaRepo;
    private final SubPresupuestoRepository SubPresRepo;

    private final PartidaService partidaService;
    private final GGItemService ggItemService;
    private final SPFormulaService spFormulaService;

    /**
     * Recalcula todas las hojas de un SubPresupuesto reutilizando PartidaService.actualizarHoja(...)
     * Política Lazy: se invoca explícitamente desde un endpoint.
     */
    @Override
    @Transactional
    public RecalculoResultDto recalcularSubPresupuesto(Long subPresupuestoId) {
        SubPresupuesto sp = SubPresRepo.findById(subPresupuestoId)
                .orElseThrow(() -> new DomainException("subPresupuesto no encontrado: " + subPresupuestoId));

        List<Partida> arbol = partidaRepo.findArbolBySubPresupuesto(subPresupuestoId);

        // Recalcula cada HOJA (sin cambiar sus atributos) para que tome el precio_base vigente
        for (Partida p : arbol) {
            if (p.getTipo() == TipoPartida.HOJA) {
                var cmd = new HojaUpdateCmd(
                        p.getId(),
                        null, // metrado
                        null, // rendimiento
                        null, // unidadId
                        null, // padreId
                        null, // orden
                        null  // lineas (sin cambios)
                );
                partidaService.actualizarHoja(cmd);
            }
        }

        // Lee CD del SP ya materializado por las llamadas anteriores
        BigDecimal cd = SubPresRepo.findById(sp.getId())
                .map(SubPresupuesto::getCdTotal)
                .orElse(BigDecimal.ZERO);

        // Recalcula GG y Pie con el CD resultante
        BigDecimal gg = ggItemService.recalcularGG(sp.getId(), cd);

        Map<String, BigDecimal> base = new HashMap<>();
        base.put("CD", cd);
        base.put("GG", gg);
        Map<String, BigDecimal> pie = spFormulaService.evaluarPie(sp.getId(), base);

        return new RecalculoResultDto(sp.getId(), cd, gg, pie);
    }
}
