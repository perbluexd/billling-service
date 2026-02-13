package com.cvanguardistas.billing_service.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PrecioService {
    BigDecimal precioVigente(Long insumoId, LocalDate fechaBase);
}
