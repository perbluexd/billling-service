// src/main/java/com/cvanguardistas/billing_service/web/audit/Auditable.java
package com.cvanguardistas.billing_service.web.audit;

import com.cvanguardistas.billing_service.entities.AccionAuditoria;

import java.lang.annotation.*;

/**
 * Marca un método de servicio para ser auditado.
 * - entityId:     SpEL sobre parámetros (p.ej. "#id", "#cmd.partidaId")
 * - entityIdFromResult: SpEL sobre el resultado (p.ej. "#result.id" o "#result")
 * - logPayload:   si serializar args y result (recortado por el Aspecto)
 * - razon:        etiqueta opcional (motivo libre)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    String entidad();                          // "Presupuesto", "Partida", etc.
    AccionAuditoria accion();                  // enum de acciones
    String entityId() default "";              // SpEL: "#id", "#cmd.partidaId"
    String entityIdFromResult() default "";    // SpEL: "#result.id" o "#result"
    boolean logPayload() default true;         // serializar args/result
    String razon() default "";                 // opcional
}
