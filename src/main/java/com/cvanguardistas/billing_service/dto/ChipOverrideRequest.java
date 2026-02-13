// src/main/java/com/cvanguardistas/billing_service/dto/ChipOverrideRequest.java
package com.cvanguardistas.billing_service.dto;

import java.math.BigDecimal;

public record ChipOverrideRequest(
        Boolean usarOverride,
        BigDecimal unitarioOverride
) {}
