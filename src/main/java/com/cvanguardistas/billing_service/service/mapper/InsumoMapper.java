package com.cvanguardistas.billing_service.service.mapper;

import com.cvanguardistas.billing_service.dto.InsumoListItemDto;
import com.cvanguardistas.billing_service.entities.Insumo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper reservado para catálogos de Insumo (Sprint 2/4).
 * Integra el DTO de listado de precios estándar.
 */
@Mapper(config = MapStructConfig.class)
public interface InsumoMapper {

    @Mapping(target = "unidadId",
            expression = "java(insumo.getUnidad() != null ? insumo.getUnidad().getId() : null)")
    @Mapping(target = "unidadCodigo",
            expression = "java(insumo.getUnidad() != null ? insumo.getUnidad().getCodigo() : null)")
    InsumoListItemDto toListItem(Insumo insumo);
}
