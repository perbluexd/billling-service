package com.cvanguardistas.billing_service.web;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements Filter {

    @Value("${app.security.tracing.correlation-header:X-Correlation-Id}")
    private String headerName;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String cid = req.getHeader(headerName);
        if (cid == null || cid.isBlank()) {
            cid = UUID.randomUUID().toString();
        }
        MDC.put("correlationId", cid);
        ((HttpServletResponse) response).setHeader(headerName, cid);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
