package com.cvanguardistas.billing_service.web.audit;

import com.cvanguardistas.billing_service.entities.AccionAuditoria;
import com.cvanguardistas.billing_service.entities.Usuario;
import com.cvanguardistas.billing_service.service.AuditoriaService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditoriaAspect {

    private final AuditoriaService auditoriaService;  // usa REQUIRES_NEW
    private final EntityManager em;

    @Value("${app.security.tracing.correlation-header:X-Correlation-Id}")
    private String correlationHeader;

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer PARAM_DISC = new DefaultParameterNameDiscoverer();
    private static final int MAX_JSON = 32_000;

    @Around("@annotation(auditable)")
    public Object around(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        Object[] args = pjp.getArgs();

        // Ejecuta método de negocio
        Object result = pjp.proceed();

        Runnable persistTask = () -> {
            try { persist(pjp, auditable, args, result); }
            catch (Exception e) { log.warn("Auditoría falló: {}", e.toString()); }
        };

        // Graba solo tras commit de la tx principal (si existe)
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { persistTask.run(); }
            });
        } else {
            // Sin tx activa: persiste igual
            persistTask.run();
        }
        return result;
    }

    private void persist(ProceedingJoinPoint pjp, Auditable auditable, Object[] args, Object result) {
        Long userId = currentUserId();
        if (userId == null) { log.debug("Sin usuario autenticado -> no audito"); return; }

        // Referencia perezosa al usuario (sin SELECT)
        Usuario refUsuario = em.getReference(Usuario.class, userId);

        HttpServletRequest req = currentRequest();
        String ip  = req != null ? req.getRemoteAddr() : null;
        String ua  = req != null ? req.getHeader("User-Agent") : null;
        String cid = req != null ? req.getHeader(correlationHeader) : null;
        if (cid == null || cid.isBlank()) cid = UUID.randomUUID().toString();

        String entidadId = resolveEntityId(pjp, args, result, auditable);
        String payloadNuevo = null, payloadAnterior = null;

        if (auditable.logPayload()) {
            payloadNuevo = toJsonLimited(Map.of("args", args, "result", result));
            // payloadAnterior: si algún día quieres snapshot previo, aquí.
        }

        // Delegar al servicio (REQUIRES_NEW) para asegurar transacción
        auditoriaService.audit(
                userId,
                auditable.entidad(),
                entidadId,
                auditable.accion(),
                payloadAnterior,
                payloadNuevo,
                ip,
                ua,
                cid,
                ""  // razonCambio opcional
        );
    }

    private static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        try { return Long.valueOf(String.valueOf(auth.getPrincipal())); }
        catch (Exception e) { return null; }
    }

    private static HttpServletRequest currentRequest() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        return (ra instanceof ServletRequestAttributes sra) ? sra.getRequest() : null;
    }

    private String resolveEntityId(ProceedingJoinPoint pjp, Object[] args, Object result, Auditable auditable) {
        String spel = !auditable.entityId().isBlank()
                ? auditable.entityId()
                : auditable.entityIdFromResult();

        if (spel.isBlank()) return null;

        try {
            var ms = (MethodSignature) pjp.getSignature();
            var method = ms.getMethod();
            var names = PARAM_DISC.getParameterNames(method);

            var ctx = new StandardEvaluationContext();
            ctx.setVariable("result", result);

            if (names != null) {
                for (int i = 0; i < names.length; i++) {
                    ctx.setVariable(names[i], args[i]); // permite #id, #cmd, etc.
                }
            }
            Object v = PARSER.parseExpression(spel).getValue(ctx);
            return (v == null) ? null : String.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }

    private static String toJsonLimited(Object o) {
        try {
            String json = MAPPER.writeValueAsString(o);
            return json.length() > MAX_JSON ? json.substring(0, MAX_JSON) : json;
        } catch (Exception e) {
            return null;
        }
    }
}