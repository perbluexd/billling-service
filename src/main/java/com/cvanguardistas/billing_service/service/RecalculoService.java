package com.cvanguardistas.billing_service.service;

import com.cvanguardistas.billing_service.dto.RecalculoResultDto;

public interface RecalculoService {
    RecalculoResultDto recalcularSubPresupuesto(Long subPresupuestoId);
}
