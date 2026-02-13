package com.cvanguardistas.billing_service.service.mapper;

import com.cvanguardistas.billing_service.dto.CatalogoPartidaListItemDto;
import com.cvanguardistas.billing_service.entities.PlantillaPartida;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface CatalogoPartidaMapper {

    @Mapping(target = "unidadCodigo",
            expression = "java(p.getUnidad() != null ? p.getUnidad().getCodigo() : null)")
    CatalogoPartidaListItemDto toListItem(PlantillaPartida p);
}
