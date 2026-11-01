package com.ventanasyaluminios.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ventanasyaluminios.security.RateLimitFilter;
import com.ventanasyaluminios.security.RateLimiter;
import java.time.Clock;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registra los límites por IP. Va después del filtro que aplica X-Forwarded-For, para contar por la IP real del
 * cliente y no por la de nginx.
 */
@Configuration
public class RateLimitConfig {

    /** Después de ForwardedHeaderFilter (HIGHEST_PRECEDENCE) y antes de todo lo demás. */
    static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

    @Bean
    public FilterRegistrationBean<RateLimitFilter> quoteRateLimitFilter(AppProperties properties, Clock clock,
                                                                        ObjectMapper objectMapper) {
        AppProperties.Rule rule = properties.rateLimit().quotes();
        RateLimitFilter filter = new RateLimitFilter("POST", new RateLimiter(rule.maxRequests(), rule.window(), clock),
                objectMapper, "Has enviado muchas solicitudes. Intenta más tarde.");
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/v1/quotes");
        registration.setOrder(ORDER);
        registration.setName("quoteRateLimitFilter");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> loginRateLimitFilter(AppProperties properties, Clock clock,
                                                                        ObjectMapper objectMapper) {
        AppProperties.Rule rule = properties.rateLimit().login();
        RateLimitFilter filter = new RateLimitFilter("POST", new RateLimiter(rule.maxRequests(), rule.window(), clock),
                objectMapper, "Demasiados intentos. Espera un minuto.");
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/v1/auth/login");
        registration.setOrder(ORDER);
        registration.setName("loginRateLimitFilter");
        return registration;
    }
}
