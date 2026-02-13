package com.cvanguardistas.billing_service.service.mapper;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Configuración global para todos los mappers.
 */
@MapperConfig(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ACCESSOR_ONLY,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.ERROR // usa WARN si prefieres
)
public interface MapStructConfig {}
