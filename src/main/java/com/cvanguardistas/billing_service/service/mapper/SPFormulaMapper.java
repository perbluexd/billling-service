// src/main/java/com/cvanguardistas/billing_service/service/mapper/SPFormulaMapper.java
package com.cvanguardistas.billing_service.service.mapper;

import com.cvanguardistas.billing_service.dto.SPFormulaDto;
import com.cvanguardistas.billing_service.entities.SPFormula;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface SPFormulaMapper {

    @Mapping(target = "subPresupuestoId", source = "subPresupuesto.id")
    SPFormulaDto toDto(SPFormula e);

    List<SPFormulaDto> toDtoList(List<SPFormula> list);
}
