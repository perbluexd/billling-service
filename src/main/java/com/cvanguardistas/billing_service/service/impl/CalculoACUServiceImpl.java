package com.cvanguardistas.billing_service.service.impl;


import com.cvanguardistas.billing_service.dto.ChipDto;
import com.cvanguardistas.billing_service.dto.HojaCalcRequest;
import com.cvanguardistas.billing_service.dto.HojaDto;
import com.cvanguardistas.billing_service.dto.LineaACUDto;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.service.CalculoACUService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CalculoACUServiceImpl implements CalculoACUService {

    private static final int SCALE_LINEA = 6;
    private static final int SCALE_MONTO = 2;

    @Override
    public HojaDto calcular(HojaCalcRequest in) {
        // Validaciones mínimas de contrato (defensivas)
        if (in.metrado() == null || in.metrado().compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Metrado inválido");
        }
        if (in.rendimiento() == null || in.rendimiento().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Rendimiento debe ser > 0");
        }
        if (in.jornadaEfectiva() == null || in.jornadaEfectiva().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Jornada efectiva debe ser > 0");
        }

        List<LineaACUDto> lineasOut = new ArrayList<>();

        for (var lin : in.lineas()) {
            boolean depende = Boolean.TRUE.equals(lin.dependeDeRendimiento());
            if (depende && (lin.cuadrillaFrac() == null || lin.cuadrillaFrac().compareTo(BigDecimal.ZERO) <= 0)) {
                throw new DomainException("Regla depende=true requiere cuadrilla_frac > 0");
            }
            if (!depende && (lin.cantidadFija() == null || lin.cantidadFija().compareTo(BigDecimal.ZERO) <= 0)) {
                throw new DomainException("Regla depende=false requiere cantidad_fija > 0");
            }

            BigDecimal cantidad = depende
                    ? lin.cuadrillaFrac().multiply(in.jornadaEfectiva())
                    .divide(in.rendimiento(), SCALE_LINEA, RoundingMode.HALF_UP)
                    : lin.cantidadFija().setScale(SCALE_LINEA, RoundingMode.HALF_UP);

            BigDecimal pu = lin.puOverride() != null && Boolean.TRUE.equals(lin.usarPuOverride())
                    ? lin.puOverride()
                    : // Si llegaste aquí con request “puro”, ya deberías traer PU resuelto desde PartidaService
                    (lin.puOverride() != null ? lin.puOverride() : BigDecimal.ZERO);

            BigDecimal parcial = cantidad.multiply(pu).setScale(SCALE_MONTO, RoundingMode.HALF_UP);

            lineasOut.add(new LineaACUDto(
                    null, lin.insumoId(), lin.categoriaCostoId(), cantidad, pu, parcial
            ));
        }

        // Chips por categoría (unitario por 1 unidad y total = unitario * metrado)
        Map<Long, BigDecimal> sumParcialPorCategoria = lineasOut.stream()
                .collect(Collectors.groupingBy(
                        LineaACUDto::categoriaCostoId,
                        Collectors.mapping(LineaACUDto::parcial, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        List<ChipDto> chips = new ArrayList<>();
        for (var e : sumParcialPorCategoria.entrySet()) {
            Long categoria = e.getKey();
            BigDecimal parcialTotal = e.getValue(); // por todo el metrado actual
            BigDecimal unitario = (in.metrado().compareTo(BigDecimal.ZERO) == 0)
                    ? BigDecimal.ZERO
                    : parcialTotal.divide(in.metrado(), SCALE_MONTO, RoundingMode.HALF_UP);
            BigDecimal total = unitario.multiply(in.metrado()).setScale(SCALE_MONTO, RoundingMode.HALF_UP);
            chips.add(new ChipDto(categoria, unitario, total));
        }

        // CU (puro: suma de todos los unitarios; la inclusión por categoría se aplicará en PartidaService)
        BigDecimal cu = chips.stream()
                .map(ChipDto::unitarioCalc)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE_MONTO, RoundingMode.HALF_UP);

        BigDecimal parcialHoja = cu.multiply(in.metrado()).setScale(SCALE_MONTO, RoundingMode.HALF_UP);

        return new HojaDto(
                null, null, in.rendimiento(), in.metrado(), cu, parcialHoja, chips, lineasOut
        );
    }
}
