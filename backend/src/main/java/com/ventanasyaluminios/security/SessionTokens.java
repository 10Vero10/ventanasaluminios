package com.ventanasyaluminios.security;

import com.ventanasyaluminios.config.AppProperties;
import com.ventanasyaluminios.entity.AdminUser;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * Emite el JWT de sesión del panel y arma la cookie que lo transporta: httpOnly (el JavaScript no la lee), SameSite=Lax,
 * Secure según configuración y limitada a {@code /api}.
 */
@Component
public class SessionTokens {

    public static final String COOKIE_NAME = "vya_session";
    public static final String ISSUER = "ventanasyaluminios";
    static final String VERSION_CLAIM = "ver";
    static final String EMAIL_CLAIM = "email";

    private final JwtEncoder encoder;
    private final AppProperties.Auth auth;
    private final Clock clock;

    public SessionTokens(JwtEncoder encoder, AppProperties properties, Clock clock) {
        this.encoder = encoder;
        this.auth = properties.auth();
        this.clock = clock;
    }

    /** Encabezado Set-Cookie con una sesión nueva para {@code admin}. */
    public String issueCookie(AdminUser admin) {
        Instant now = Instant.now(clock);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(admin.getId()))
                .issuedAt(now)
                .expiresAt(now.plus(auth.sessionDuration()))
                .claim(EMAIL_CLAIM, admin.getEmail())
                .claim(VERSION_CLAIM, admin.getTokenVersion())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return cookie(token, auth.sessionDuration()).toString();
    }

    /** Encabezado Set-Cookie que borra la sesión del navegador. */
    public String clearCookie() {
        return cookie("", Duration.ZERO).toString();
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(auth.cookieSecure())
                .sameSite("Lax")
                .path("/api")
                .maxAge(maxAge)
                .build();
    }
}
