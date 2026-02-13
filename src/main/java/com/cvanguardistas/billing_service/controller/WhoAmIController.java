package com.cvanguardistas.billing_service.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/me")
public class WhoAmIController {

    @GetMapping
    public Map<String, Object> me(Authentication auth) {
        return Map.of(
                "userId", auth.getName(),                  // sujeto (id) que pusimos en el token
                "authorities", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).collect(Collectors.toList())
        );
    }
}
