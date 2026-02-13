// src/main/java/com/cvanguardistas/billing_service/service/GGItemService.java
package com.cvanguardistas.billing_service.service;

import java.math.BigDecimal;

public interface GGItemService {
    /** Recalcula y materializa GG del SubPresupuesto. Devuelve el GG total. */
    BigDecimal recalcularGG(Long subPresupuestoId, BigDecimal cdActual);
}
