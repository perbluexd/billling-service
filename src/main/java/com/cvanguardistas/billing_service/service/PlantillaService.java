// com.cvanguardistas.billing_service.service.PlantillaService
package com.cvanguardistas.billing_service.service;

import com.cvanguardistas.billing_service.dto.PlantillaResumenDto;

import java.util.List;

public interface PlantillaService {
    List<PlantillaResumenDto> listar();
    Long instanciar(Long plantillaId, Long presupuestoId);
}
