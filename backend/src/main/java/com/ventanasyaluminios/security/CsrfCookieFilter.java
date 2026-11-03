package com.ventanasyaluminios.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Fuerza a generar el token CSRF en cada respuesta de la API para que el navegador reciba la cookie XSRF-TOKEN. El frontend
 * (axios) la devuelve en el encabezado X-XSRF-TOKEN en cada cambio del panel.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    /**
     * El token solo hace falta para llamar a la API. En los archivos del sitio no se envía: algunos se guardan en caché
     * pública por un año y no deben llevar la cookie de nadie.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            token.getToken();
        }
        chain.doFilter(request, response);
    }
}
