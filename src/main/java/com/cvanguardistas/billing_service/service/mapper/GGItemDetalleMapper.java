// src/main/java/com/cvanguardistas/billing_service/service/mapper/GGItemDetalleMapper.java
package com.cvanguardistas.billing_service.service.mapper;

import com.cvanguardistas.billing_service.dto.GGItemDetalleDto;
import com.cvanguardistas.billing_service.entities.GGItemDetalle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GGItemDetalleMapper {
    @Mapping(target = "ggItemId", source = "ggItem.id")
    GGItemDetalleDto toDto(GGItemDetalle e);
}
