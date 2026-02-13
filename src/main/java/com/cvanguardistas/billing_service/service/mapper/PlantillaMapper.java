// com.cvanguardistas.billing_service.service.mapper.PlantillaMapper
package com.cvanguardistas.billing_service.service.mapper;

import com.cvanguardistas.billing_service.dto.PlantillaResumenDto;
import com.cvanguardistas.billing_service.entities.Plantilla;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface PlantillaMapper {
    PlantillaResumenDto toResumen(Plantilla p);
}
