// src/main/java/com/cvanguardistas/billing_service/service/impl/GGItemServiceImpl.java
package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.entities.FormatoGG;
import com.cvanguardistas.billing_service.entities.GGItem;
import com.cvanguardistas.billing_service.entities.GGItemDetalle;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.GGItemDetalleRepository;
import com.cvanguardistas.billing_service.repository.GGItemRepository;
import com.cvanguardistas.billing_service.repository.SubPresupuestoRepository;
import com.cvanguardistas.billing_service.service.GGItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GGItemServiceImpl implements GGItemService {

    private static final int SCALE_MONTO = 2;

    private final GGItemRepository itemRepo;
    private final GGItemDetalleRepository detRepo;
    private final SubPresupuestoRepository spRepo;

    @Override
    @Transactional
    public BigDecimal recalcularGG(Long subPresupuestoId, BigDecimal cdActual) {
        List<GGItem> items = itemRepo.findBySubPresupuestoIdOrderByOrdenAsc(subPresupuestoId);
        BigDecimal ggTotal = BigDecimal.ZERO;

        for (GGItem item : items) {
            List<GGItemDetalle> detalles = detRepo.findByGgItemIdOrderByOrdenAsc(item.getId());
            for (GGItemDetalle d : detalles) {
                BigDecimal parcial;
                if (item.getFormato() == FormatoGG.EN_FUNCION_CD) {
                    BigDecimal pct = (d.getPorcentaje() == null) ? BigDecimal.ZERO : d.getPorcentaje();
                    // pct se espera en fracción (ej. 0.10 para 10%)
                    parcial = cdActual.multiply(pct);
                } else if (item.getFormato() == FormatoGG.ESTANDAR
                        || item.getFormato() == FormatoGG.PERSONAL
                        || item.getFormato() == FormatoGG.ENSAYOS) {
                    BigDecimal cant = (d.getCantidad() == null) ? BigDecimal.ZERO : d.getCantidad();
                    BigDecimal precio = (d.getPrecio() == null) ? BigDecimal.ZERO : d.getPrecio();
                    parcial = cant.multiply(precio);
                } else {
                    throw new DomainException("Formato GG no soportado: " + item.getFormato());
                }

                parcial = parcial.setScale(SCALE_MONTO, RoundingMode.HALF_UP);
                d.setParcial(parcial);
                detRepo.save(d);

                ggTotal = ggTotal.add(parcial);
            }
        }

        // Si más adelante decides materializar GG total en SubPresupuesto, aquí es el sitio:
        // var sp = spRepo.findById(SubPresupuestoId).orElseThrow(...);
        // sp.setGgTotal(ggTotal);
        // spRepo.save(sp);

        return ggTotal.setScale(SCALE_MONTO, RoundingMode.HALF_UP);
    }
}
