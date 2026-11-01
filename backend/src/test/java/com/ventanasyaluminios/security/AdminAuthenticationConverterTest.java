package com.ventanasyaluminios.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ventanasyaluminios.entity.AdminUser;
import com.ventanasyaluminios.repository.AdminUserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.test.util.ReflectionTestUtils;

class AdminAuthenticationConverterTest {

    private final AdminUserRepository repository = mock(AdminUserRepository.class);
    private final AdminAuthenticationConverter converter = new AdminAuthenticationConverter(repository);

    @Test
    void unAdministradorQueYaCambioLaContrasenaTieneRolAdmin() {
        AdminUser admin = admin(false);
        admin.changePassword("hash");
        when(repository.findById(1L)).thenReturn(Optional.of(admin));

        var authentication = converter.convert(jwt("1", 1));

        assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly(AdminAuthenticationConverter.ROLE_ADMIN);
        assertThat(((AuthenticatedAdmin) authentication.getPrincipal()).id()).isEqualTo(1L);
    }

    @Test
    void conContrasenaTemporalSoloTieneElRolDeCambio() {
        when(repository.findById(1L)).thenReturn(Optional.of(admin(true)));

        assertThat(converter.convert(jwt("1", 0)).getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly(AdminAuthenticationConverter.ROLE_PASSWORD_CHANGE_REQUIRED);
    }

    @Test
    void unTokenDeUnaVersionAnteriorOSinUsuarioSeRechaza() {
        AdminUser admin = admin(false);
        admin.revokeSessions();
        when(repository.findById(1L)).thenReturn(Optional.of(admin));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> converter.convert(jwt("1", 0))).isInstanceOf(InvalidBearerTokenException.class);
        assertThatThrownBy(() -> converter.convert(jwt("2", 0))).isInstanceOf(InvalidBearerTokenException.class);
        assertThatThrownBy(() -> converter.convert(jwt("no-es-un-id", 0))).isInstanceOf(InvalidBearerTokenException.class);
    }

    @Test
    void elTokenSoloSeLeeDeLaCookieEnRutasConSesion() {
        assertThat(SessionCookieTokenResolver.requiresSession("/api/v1/admin/quotes")).isTrue();
        assertThat(SessionCookieTokenResolver.requiresSession("/api/v1/auth/me")).isTrue();
        assertThat(SessionCookieTokenResolver.requiresSession("/api/v1/auth/login")).isFalse();
        assertThat(SessionCookieTokenResolver.requiresSession("/api/v1/site")).isFalse();
        assertThat(SessionCookieTokenResolver.requiresSession("/api/v1/administrador")).isFalse();
    }

    private static AdminUser admin(boolean mustChangePassword) {
        AdminUser admin = new AdminUser("admin@empresa.com", "hash", mustChangePassword, Instant.now());
        ReflectionTestUtils.setField(admin, "id", 1L);
        return admin;
    }

    private static Jwt jwt(String subject, int version) {
        return Jwt.withTokenValue("token").header("alg", "HS256").subject(subject).claim("ver", version).build();
    }
}
