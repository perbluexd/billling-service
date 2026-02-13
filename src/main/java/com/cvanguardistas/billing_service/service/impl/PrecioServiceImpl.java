package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.entities.InsumoPrecioHist;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.InsumoPrecioHistRepository;
import com.cvanguardistas.billing_service.repository.InsumoRepository;
import com.cvanguardistas.billing_service.service.PrecioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PrecioServiceImpl implements PrecioService {

    private final InsumoRepository insumoRepo;
    private final InsumoPrecioHistRepository histRepo;

    @Override
    public BigDecimal precioVigente(Long insumoId, LocalDate fechaBase) {
        var insumo = insumoRepo.findById(insumoId)
                .orElseThrow(() -> new DomainException("Insumo no encontrado: " + insumoId));

        // 1) Si tienes precio base en el insumo, úsalo
        if (insumo.getPrecioBase() != null) {
            return insumo.getPrecioBase();
        }

        // 2) Si no, toma el histórico vigente a la fecha (fin de día)
        LocalDateTime hasta = (fechaBase != null ? fechaBase : LocalDate.now())
                .atTime(23, 59, 59);

        return histRepo
                .findTopByInsumo_IdAndVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(insumoId, hasta)
                .map(InsumoPrecioHist::getPrecio)
                .orElseThrow(() -> new DomainException(
                        String.format(
                                "No existe precio para insumo %d (ni precio_base ni histórico vigente a %s)",
                                insumoId, (fechaBase != null ? fechaBase : LocalDate.now()))
                ));
    }
}
