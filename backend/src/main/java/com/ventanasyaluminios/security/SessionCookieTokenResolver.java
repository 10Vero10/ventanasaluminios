package com.ventanasyaluminios.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

/**
 * Toma el JWT solo de la cookie de sesión (nunca del encabezado Authorization) y solo en las rutas que exigen
 * sesión. Así una cookie vencida no convierte en 401 las páginas públicas.
 */
public class SessionCookieTokenResolver implements BearerTokenResolver {

    @Override
    public String resolve(HttpServletRequest request) {
        if (!requiresSession(request.getRequestURI())) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> SessionTokens.COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    static boolean requiresSession(String path) {
        return path.startsWith("/api/v1/admin/") || path.equals("/api/v1/admin")
                || path.startsWith("/api/v1/auth/") && !path.equals("/api/v1/auth/login");
    }
}
