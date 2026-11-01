package com.ventanasyaluminios.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * Toma el JWT solo de la cookie de sesión (nunca del encabezado Authorization) y solo en las rutas que exigen
 * sesión. Así una cookie vencida no convierte en 401 las páginas públicas.
 */
public class SessionCookieTokenResolver {

    /** El JWT de la cookie de sesión, o {@code null} si la ruta no exige sesión o no hay cookie. */
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
