package com.cvanguardistas.billing_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static Long currentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        try { return Long.parseLong(auth.getName()); } catch (NumberFormatException e) { return null; }
    }

    public static boolean hasAuth() {
        return SecurityContextHolder.getContext().getAuthentication() != null;
    }
}
