// src/main/java/com/cvanguardistas/billing_service/service/mapper/GGItemMapper.java
package com.cvanguardistas.billing_service.service.mapper;

import com.cvanguardistas.billing_service.dto.GGItemDto;
import com.cvanguardistas.billing_service.entities.GGItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface GGItemMapper {

    @Mapping(target = "subPresupuestoId", source = "subPresupuesto.id")
    GGItemDto toDto(GGItem e);
}
