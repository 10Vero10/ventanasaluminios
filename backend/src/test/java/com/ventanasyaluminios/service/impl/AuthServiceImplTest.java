package com.ventanasyaluminios.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ventanasyaluminios.dto.request.ChangePasswordRequest;
import com.ventanasyaluminios.dto.request.LoginRequest;
import com.ventanasyaluminios.entity.AdminUser;
import com.ventanasyaluminios.exception.ApiException;
import com.ventanasyaluminios.exception.ErrorCode;
import com.ventanasyaluminios.repository.AdminUserRepository;
import com.ventanasyaluminios.security.SessionTokens;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-11-01T12:00:00Z");
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(4);

    @Mock
    private AdminUserRepository repository;

    @Mock
    private SessionTokens sessionTokens;

    private AuthServiceImpl service;
    private AdminUser admin;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(repository, ENCODER, sessionTokens, Clock.fixed(NOW, ZoneOffset.UTC));
        admin = new AdminUser("Admin@Empresa.com", ENCODER.encode("clave-correcta-123"), true, NOW);
        ReflectionTestUtils.setField(admin, "id", 1L);
    }

    @Test
    void ingresaConCorreoSinImportarMayusculasYRegistraElIngreso() {
        when(repository.findByEmail("admin@empresa.com")).thenReturn(Optional.of(admin));
        when(sessionTokens.issueCookie(admin)).thenReturn("vya_session=token");

        var session = service.login(new LoginRequest("  ADMIN@empresa.com ", "clave-correcta-123"));

        assertThat(session.sessionCookie()).isEqualTo("vya_session=token");
        assertThat(session.admin().mustChangePassword()).isTrue();
        assertThat(admin.getLastLoginAt()).isEqualTo(NOW);
    }

    @Test
    void contrasenaEquivocadaYCorreoInexistenteRespondenLoMismo() {
        when(repository.findByEmail(anyString())).thenReturn(Optional.of(admin), Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("admin@empresa.com", "otra-clave")))
                .isInstanceOf(ApiException.class).hasMessage("Correo o contraseña incorrectos.");
        assertThatThrownBy(() -> service.login(new LoginRequest("nadie@empresa.com", "clave-correcta-123")))
                .isInstanceOf(ApiException.class).hasMessage("Correo o contraseña incorrectos.")
                .extracting(ex -> ((ApiException) ex).getCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void unaContrasenaDeMasDe72BytesNoSeCompara() {
        when(repository.findByEmail("admin@empresa.com")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.login(new LoginRequest("admin@empresa.com", "ñ".repeat(40))))
                .isInstanceOf(ApiException.class).hasMessage("Correo o contraseña incorrectos.");
    }

    @Test
    void cambiarLaContrasenaQuitaLaObligacionEInvalidaLasSesionesAnteriores() {
        when(repository.findById(1L)).thenReturn(Optional.of(admin));
        when(sessionTokens.issueCookie(any())).thenReturn("vya_session=nuevo");
        int versionBefore = admin.getTokenVersion();

        var session = service.changePassword(1L, new ChangePasswordRequest("clave-correcta-123", "una-clave-nueva-larga"));

        assertThat(session.admin().mustChangePassword()).isFalse();
        assertThat(admin.getTokenVersion()).isEqualTo(versionBefore + 1);
        assertThat(ENCODER.matches("una-clave-nueva-larga", admin.getPasswordHash())).isTrue();
        verify(repository).flush();
    }

    @Test
    void cambiarLaContrasenaExigeLaActualYUnaDistinta() {
        when(repository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.changePassword(1L, new ChangePasswordRequest("equivocada", "una-clave-nueva-larga")))
                .hasMessage("La contraseña actual no es correcta.");
        assertThatThrownBy(() -> service.changePassword(1L,
                new ChangePasswordRequest("clave-correcta-123", "clave-correcta-123")))
                .hasMessage("La contraseña nueva debe ser distinta de la actual.");
    }

    @Test
    void cerrarSesionSubeLaVersionDelToken() {
        when(repository.findById(1L)).thenReturn(Optional.of(admin));

        service.logout(1L);

        assertThat(admin.getTokenVersion()).isEqualTo(1);
    }
}
