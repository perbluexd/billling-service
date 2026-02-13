// src/main/java/com/cvanguardistas/billing_service/service/ReporteService.java
package com.cvanguardistas.billing_service.service;

public interface ReporteService {
    byte[] exportarDesagregadoXlsx(Long SubPresupuestoId);
    byte[] exportarInsumosXlsx(Long subPresupuestoId);
}
