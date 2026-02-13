package com.cvanguardistas.billing_service.service.mapper;

import com.cvanguardistas.billing_service.dto.CatalogoPartidaDto;
import com.cvanguardistas.billing_service.entities.PartidaCatalogo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface CatalogoMapper {
    // Nombres iguales: id, nombre, codigo, tipo → no necesitas @Mapping
    CatalogoPartidaDto toDto(PartidaCatalogo e);
    List<CatalogoPartidaDto> toDtos(List<PartidaCatalogo> list);
}
