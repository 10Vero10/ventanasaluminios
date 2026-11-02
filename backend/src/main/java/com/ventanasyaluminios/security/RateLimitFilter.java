package com.ventanasyaluminios.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ventanasyaluminios.dto.response.ErrorResponse;
import com.ventanasyaluminios.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Aplica un {@link RateLimiter} por IP a un método y una ruta. Al pasar el límite responde 429 con el formato de
 * error de la API y {@code Retry-After}, sin llegar al controller. La IP sale de {@code getRemoteAddr}: detrás de nginx
 * es la del cliente, porque nginx reemplaza X-Forwarded-For y Tomcat solo lo acepta de proxies internos.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private final String method;
    private final RateLimiter limiter;
    private final ObjectMapper objectMapper;
    private final String message;
    private final String trustedProxySecret;

    public RateLimitFilter(String method, RateLimiter limiter, ObjectMapper objectMapper, String message,
                           String trustedProxySecret) {
        this.method = method;
        this.limiter = limiter;
        this.objectMapper = objectMapper;
        this.message = message;
        this.trustedProxySecret = trustedProxySecret;
    }

    /** La ruta la decide el registro del filtro; aquí solo se filtra por método. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !method.equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Duration retryAfter = limiter.acquire(ClientKey.of(ClientAddress.of(request, trustedProxySecret)));
        if (retryAfter.isZero()) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(ErrorCode.RATE_LIMITED.getStatus().value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(Math.max(1, retryAfter.toSeconds())));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new ErrorResponse(message, ErrorCode.RATE_LIMITED.name()));
    }
}
