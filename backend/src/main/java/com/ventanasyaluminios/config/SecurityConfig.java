package com.ventanasyaluminios.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.ventanasyaluminios.repository.AdminUserRepository;
import com.ventanasyaluminios.security.AdminAuthenticationConverter;
import com.ventanasyaluminios.security.CsrfCookieFilter;
import com.ventanasyaluminios.security.SecurityErrorResponses;
import com.ventanasyaluminios.security.SessionCookieAuthenticationFilter;
import com.ventanasyaluminios.security.SessionCookieTokenResolver;
import com.ventanasyaluminios.security.SessionTokens;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Seguridad del API (ADR-015): sesión sin estado con JWT HS256 en cookie httpOnly, rol ADMIN para el panel, CSRF con
 * cookie XSRF-TOKEN para todo cambio autenticado y respuestas 401/403 en el formato de error de la API. La cookie la
 * autentica {@link SessionCookieAuthenticationFilter}, no oauth2-resource-server, para no perder la protección CSRF.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain apiSecurity(HttpSecurity http, AdminUserRepository adminUserRepository,
                                           JwtDecoder jwtDecoder, ObjectMapper objectMapper) throws Exception {
        var entryPoint = SecurityErrorResponses.entryPoint(objectMapper);
        var accessDenied = SecurityErrorResponses.accessDeniedHandler(objectMapper);

        // El token CSRF se compara tal cual (sin la máscara XOR), como lo envía axios desde la cookie.
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null);

        http
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfHandler)
                        // Sin sesión previa que proteger: formulario público y login.
                        .ignoringRequestMatchers("/api/v1/quotes", "/api/v1/auth/login"))
                .addFilterAfter(new SessionCookieAuthenticationFilter(new SessionCookieTokenResolver(), jwtDecoder,
                        new AdminAuthenticationConverter(adminUserRepository)), CsrfFilter.class)
                .addFilterAfter(new CsrfCookieFilter(), SessionCookieAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/admin/**").hasAuthority(AdminAuthenticationConverter.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDenied));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public JwtEncoder jwtEncoder(AppProperties properties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(properties)));
    }

    /** Verifica firma HS256, vencimiento y emisor; la vigencia contra la base la revisa el conversor. */
    @Bean
    public JwtDecoder jwtDecoder(AppProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey(properties))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(SessionTokens.ISSUER)));
        return decoder;
    }

    private static SecretKey secretKey(AppProperties properties) {
        return new SecretKeySpec(properties.auth().jwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
