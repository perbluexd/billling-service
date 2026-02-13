// src/main/java/com/cvanguardistas/billing_service/service/SPFormulaService.java
package com.cvanguardistas.billing_service.service;

import java.math.BigDecimal;
import java.util.Map;

public interface SPFormulaService {
    /**
     * Evalúa todas las fórmulas del SubPresupuesto en orden y persiste sus valores.
     * @param subPresupuestoId SP
     * @param variablesBase mapa con variables iniciales (ej: CD, GG)
     * @return mapa final variable→valor (incluye variables calculadas como TOTAL si existen)
     */
    Map<String, BigDecimal> evaluarPie(Long subPresupuestoId, Map<String, BigDecimal> variablesBase);
}
