package com.cvanguardistas.billing_service.service.mapper;

import com.cvanguardistas.billing_service.dto.SubPresupuestoResumenDto;
import com.cvanguardistas.billing_service.entities.SubPresupuesto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapea SubPresupuesto → SubPresupuestoResumenDto.
 * Asume que los totales están materializados en la entidad.
 */
@Mapper(config = MapStructConfig.class)
public interface SubPresupuestoMapper {

    @Mapping(target = "subPresupuestoId", source = "id")
    // Las siguientes líneas no serían estrictamente necesarias si el nombre ya coincide,
    // pero se dejan explícitas para mayor claridad.
    @Mapping(target = "moTotal", source = "moTotal")
    @Mapping(target = "mtTotal", source = "mtTotal")
    @Mapping(target = "eqTotal", source = "eqTotal")
    @Mapping(target = "scTotal", source = "scTotal")
    @Mapping(target = "spTotal", source = "spTotal")
    @Mapping(target = "cdTotal", source = "cdTotal")
    SubPresupuestoResumenDto toResumen(SubPresupuesto sp);
}
