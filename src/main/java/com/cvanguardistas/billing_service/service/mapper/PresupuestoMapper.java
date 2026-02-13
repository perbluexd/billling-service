package com.cvanguardistas.billing_service.service.mapper;

import com.cvanguardistas.billing_service.dto.PresupuestoDetalleDto;
import com.cvanguardistas.billing_service.dto.PresupuestoListItemDto;
import com.cvanguardistas.billing_service.dto.SubPresupuestoResumenDto;
import com.cvanguardistas.billing_service.entities.Presupuesto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PresupuestoMapper {

    public PresupuestoListItemDto toListItem(Presupuesto p, BigDecimal totalCd) {
        return new PresupuestoListItemDto(
                p.getId(),
                p.getNombre(),
                p.getEstado(),
                p.getCreadoEn(),
                totalCd
        );
    }

    public PresupuestoDetalleDto toDetalle(Presupuesto p,
                                           BigDecimal totalCd,
                                           List<SubPresupuestoResumenDto> Subres) {
        return new PresupuestoDetalleDto(
                p.getId(),
                p.getNombre(),
                p.getEstado(),
                p.getFechaBase(),
                p.getMoneda(),
                p.getJornadaHoras(),
                p.getCreadoEn(),
                p.getActualizadoEn(),
                totalCd,
                Subres
        );
    }
}
