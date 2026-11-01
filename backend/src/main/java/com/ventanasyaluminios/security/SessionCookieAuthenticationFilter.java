package com.ventanasyaluminios.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica la solicitud con el JWT de la cookie de sesión. Es un filtro propio y no el de oauth2-resource-server
 * porque ese desactiva CSRF en toda solicitud con token, y aquí el token viaja en una cookie que el navegador adjunta
 * solo. Si el token falta o no es válido, la solicitud sigue sin autenticar y la autorización responde 401.
 */
public class SessionCookieAuthenticationFilter extends OncePerRequestFilter {

    private final SessionCookieTokenResolver resolver;
    private final JwtDecoder decoder;
    private final AdminAuthenticationConverter converter;

    public SessionCookieAuthenticationFilter(SessionCookieTokenResolver resolver, JwtDecoder decoder,
                                             AdminAuthenticationConverter converter) {
        this.resolver = resolver;
        this.decoder = decoder;
        this.converter = converter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = resolver.resolve(request);
        if (token != null) {
            try {
                Jwt jwt = decoder.decode(token);
                AbstractAuthenticationToken authentication = converter.convert(jwt);
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (JwtException | AuthenticationException e) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
