package com.cvanguardistas.billing_service.security;

import com.cvanguardistas.billing_service.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // Evita que Spring cree el usuario por defecto
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    // 401 JSON
    @Bean
    public AuthenticationEntryPoint jsonAuthEntryPoint() {
        return (request, response, ex) -> {
            response.setStatus(401);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Authentication required\"}");
        };
    }

    // 403 JSON
    @Bean
    public AccessDeniedHandler jsonAccessDeniedHandler() {
        return (request, response, ex) -> {
            response.setStatus(403);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"forbidden\",\"message\":\"Access denied\"}");
        };
    }

    @Bean
    public CorsProps corsProps(Environment env) {
        return new CorsProps(
                env.getProperty("app.security.cors.allowed-origins", "http://localhost:4200"),
                env.getProperty("app.security.cors.allowed-methods", "GET,POST,PATCH,PUT,DELETE,OPTIONS"),
                env.getProperty("app.security.cors.allowed-headers", "Authorization,Content-Type,X-Correlation-Id"),
                Boolean.parseBoolean(env.getProperty("app.security.cors.allow-credentials", "true"))
        );
    }

    // ⬇️ REGISTRO DEL FILTRO con JwtService + UsuarioRepository
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService,
                                                           UsuarioRepository usuarioRepo) {
        return new JwtAuthenticationFilter(jwtService, usuarioRepo);
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter,
            CorsProps corsProps,
            AuthenticationEntryPoint authEntryPoint,
            AccessDeniedHandler accessDeniedHandler
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(req -> {
                    CorsConfiguration c = new CorsConfiguration();
                    c.setAllowedOrigins(Arrays.asList(corsProps.allowedOrigins().split(",")));
                    c.setAllowedMethods(Arrays.asList(corsProps.allowedMethods().split(",")));
                    c.setAllowedHeaders(Arrays.asList(corsProps.allowedHeaders().split(",")));
                    c.setAllowCredentials(corsProps.allowCredentials());
                    return c;
                }))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(h -> h
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(reg -> reg
                        // deja públicas SOLO las necesarias
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        // swagger/actuator públicos (ajusta a tu gusto)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/actuator/health").permitAll()
                        // todo lo demás autenticado (incluye /api/auth/logout y /api/auth/change-password)
                        .anyRequest().authenticated()
                )
                // No httpBasic (usamos JWT)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    public record CorsProps(String allowedOrigins, String allowedMethods, String allowedHeaders, boolean allowCredentials) {}
}
