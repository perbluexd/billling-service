package com.cvanguardistas.billing_service.security;

import com.cvanguardistas.billing_service.repository.UsuarioRepository;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepo;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepo) {
        this.jwtService = jwtService;
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        System.out.println("==== JWT DEBUG LOGS ====");
        System.out.println("AUTH RAW HEADER: [" + authHeader + "]");

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer")) {
            System.out.println("→ No Authorization header or doesn't start with Bearer. Continuing chain.");
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.replaceFirst("^[Bb]earer\\s+", "").trim();
        System.out.println("TOKEN AFTER CLEANING: [" + token + "]");
        System.out.println("TOKEN HAS DOTS: " + token.contains("."));
        System.out.println("=========================");

        if (token.isBlank()) {
            write401(response, "Empty bearer token");
            return;
        }
        if (!token.contains(".")) {
            write401(response, "Invalid bearer token format (missing parts)");
            return;
        }

        try {
            // 1) Parsear y validar
            JWTClaimsSet claims = jwtService.parseAndValidate(token);

            // 2) Sub (userId) obligatorio
            String userId = claims.getSubject();
            if (userId == null || userId.isBlank()) {
                write401(response, "Token without Subject");
                return;
            }

            // 3) Validar iat vs pwdChangedAt (si existe)
            var userOpt = usuarioRepo.findById(Long.parseLong(userId));
            if (userOpt.isPresent()) {
                var pwdChangedAt = userOpt.get().getPwdChangedAt(); // OffsetDateTime/LocalDateTime/etc.
                var iatDate = claims.getIssueTime();                 // java.util.Date o null

                System.out.println("[JWT] iat class: " + (iatDate == null ? "null" : iatDate.getClass().getName()));
                System.out.println("[JWT] pwdChangedAt class: " + (pwdChangedAt == null ? "null" : pwdChangedAt.getClass().getName()));

                Instant iatInstant = (iatDate != null) ? iatDate.toInstant() : null;
                Instant pwdChangedInstant = toInstantSafe(pwdChangedAt);

                if (pwdChangedInstant != null) {
                    System.out.println("[JWT] iat=" + iatInstant + " vs pwdChangedAt=" + pwdChangedInstant);
                }

                // Si iat es nulo o anterior al cambio de password → 401 explícito
                if (iatInstant == null || (pwdChangedInstant != null && iatInstant.isBefore(pwdChangedInstant))) {
                    System.out.println("[JWT] SKIP AUTH (401): token iat older than password change");
                    write401(response, "Token iat older than password change");
                    return;
                }
            }

            // 4) Autoridades desde claim "roles"
            Object rolesObj = claims.getClaim("roles");
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            if (rolesObj instanceof List<?> list) {
                for (Object o : list) {
                    if (o == null) continue;
                    String r = String.valueOf(o).trim();
                    if (r.isEmpty()) continue;
                    String roleName = r.startsWith("ROLE_") ? r : ("ROLE_" + r);
                    authorities.add(new SimpleGrantedAuthority(roleName));
                }
            }

            // 5) Construir Jwt + Authentication y setear en el SecurityContext
            SignedJWT signed = SignedJWT.parse(token);
            Map<String, Object> headers = signed.getHeader().toJSONObject();
            Map<String, Object> claimMap = claims.toJSONObject();

            Instant issuedAt  = claims.getIssueTime() != null ? claims.getIssueTime().toInstant() : null;
            Instant expiresAt = claims.getExpirationTime() != null ? claims.getExpirationTime().toInstant() : null;

            Jwt.Builder jwtBuilder = Jwt.withTokenValue(token)
                    .headers(h -> h.putAll(headers))
                    .claims(c -> c.putAll(claimMap));

            if (issuedAt != null)  jwtBuilder.issuedAt(issuedAt);
            if (expiresAt != null) jwtBuilder.expiresAt(expiresAt);

            Jwt jwt = jwtBuilder.build();

            JwtAuthenticationToken authentication =
                    new JwtAuthenticationToken(jwt, authorities, claims.getSubject());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("[JWT] AUTH OK -> userId=" + claims.getSubject() + ", roles=" + authorities);

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            System.out.println("JWT PARSE/VALIDATE error: " + ex.getClass().getName() + " - " + ex.getMessage());
            ex.printStackTrace();
            write401(response, "Invalid or expired token");
            return;
        }

        // IMPORTANTÍSIMO: dejar fuera del try/catch
        chain.doFilter(request, response);
    }

    /** Conversión segura a Instant para distintos tipos de fecha */
    private static Instant toInstantSafe(Object ts) {
        if (ts == null) return null;
        try {
            if (ts instanceof Instant i) return i;
            if (ts instanceof java.util.Date d) return d.toInstant();
            if (ts instanceof OffsetDateTime odt) return odt.toInstant();
            if (ts instanceof ZonedDateTime zdt) return zdt.toInstant();
            if (ts instanceof LocalDateTime ldt) return ldt.atZone(ZoneId.systemDefault()).toInstant();
            System.out.println("JWT WARN: Unsupported datetime type for pwdChangedAt: " + ts.getClass());
            return null;
        } catch (Exception e) {
            System.out.println("JWT WARN: toInstantSafe failed for " + ts.getClass() + " -> " + e);
            return null;
        }
    }

    private void write401(HttpServletResponse resp, String message) throws IOException {
        if (resp.isCommitted()) return;
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"" + message + "\"}");
    }
}
