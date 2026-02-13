// src/main/java/com/cvanguardistas/billing_service/service/PartidaChipService.java
package com.cvanguardistas.billing_service.service;

import java.math.BigDecimal;

public interface PartidaChipService {
    void setOverride(Long partidaId, Long categoriaId, Boolean usarOverride, BigDecimal unitarioOverride);
}
