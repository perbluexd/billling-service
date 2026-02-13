package com.cvanguardistas.billing_service.service;

import com.cvanguardistas.billing_service.dto.HojaCalcRequest;
import com.cvanguardistas.billing_service.dto.HojaDto;

public interface CalculoACUService {
    HojaDto calcular(HojaCalcRequest request);
}
