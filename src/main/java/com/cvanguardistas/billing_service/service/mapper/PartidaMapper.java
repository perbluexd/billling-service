package com.cvanguardistas.billing_service.service.mapper;

import com.cvanguardistas.billing_service.dto.ChipDto;
import com.cvanguardistas.billing_service.dto.HojaDto;
import com.cvanguardistas.billing_service.dto.LineaACUDto;
import com.cvanguardistas.billing_service.entities.Partida;
import com.cvanguardistas.billing_service.entities.PartidaInsumo;
import com.cvanguardistas.billing_service.entities.PartidaTotalCategoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface PartidaMapper {

    // ---- Hoja → HojaDto ----
    @Mapping(target = "partidaId", expression = "java(hoja != null ? hoja.getId() : null)")
    @Mapping(target = "unidadId", expression = "java(hoja != null && hoja.getUnidad() != null ? hoja.getUnidad().getId() : null)")
    @Mapping(target = "rendimiento", expression = "java(hoja != null ? hoja.getRendimiento() : null)")
    @Mapping(target = "metrado", expression = "java(hoja != null ? hoja.getMetrado() : null)")
    @Mapping(target = "cu", expression = "java(hoja != null ? hoja.getCu() : null)")
    @Mapping(target = "parcial", expression = "java(hoja != null ? hoja.getParcial() : null)")
    @Mapping(target = "chips", source = "chips")
    @Mapping(target = "lineas", source = "lineas")
    HojaDto toHojaDto(Partida hoja,
                      List<PartidaInsumo> lineas,
                      List<PartidaTotalCategoria> chips);

    // ---- PartidaInsumo → LineaACUDto ----
    @Mapping(target = "partidaInsumoId", source = "id")
    @Mapping(target = "insumoId", source = "insumo.id")                  // <- aquí estaba el problema
    @Mapping(target = "categoriaCostoId", source = "categoriaCosto.id")  // <- y aquí
    @Mapping(target = "cantidad", source = "cantidad")
    @Mapping(target = "pu", source = "pu")
    @Mapping(target = "parcial", source = "parcial")
    LineaACUDto toLineaACUDto(PartidaInsumo entity);

    // ---- PartidaTotalCategoria → ChipDto ----
    @Mapping(target = "categoriaCostoId", source = "categoriaCosto.id")
    @Mapping(target = "unitarioCalc", source = "unitarioCalc")
    @Mapping(target = "totalCalc", source = "totalCalc")
    ChipDto toChipDto(PartidaTotalCategoria entity);

    // MapStruct generará automáticamente los mapeos de listas
    List<LineaACUDto> toLineaDtos(List<PartidaInsumo> entities);
    List<ChipDto> toChipDtos(List<PartidaTotalCategoria> entities);
}
