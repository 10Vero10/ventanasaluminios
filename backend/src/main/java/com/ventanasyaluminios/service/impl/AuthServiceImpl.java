package com.ventanasyaluminios.service.impl;

import com.ventanasyaluminios.dto.request.ChangePasswordRequest;
import com.ventanasyaluminios.dto.request.LoginRequest;
import com.ventanasyaluminios.dto.response.AdminUserResponse;
import com.ventanasyaluminios.entity.AdminUser;
import com.ventanasyaluminios.exception.ApiException;
import com.ventanasyaluminios.exception.ErrorCode;
import com.ventanasyaluminios.repository.AdminUserRepository;
import com.ventanasyaluminios.security.SessionTokens;
import com.ventanasyaluminios.service.AuthService;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de {@link AuthService}. */
@Service
public class AuthServiceImpl implements AuthService {

    /** BCrypt solo usa los primeros 72 bytes; una contraseña más larga se rechaza en vez de recortarse en silencio. */
    static final int MAX_PASSWORD_BYTES = 72;

    static final String INVALID_CREDENTIALS = "Correo o contraseña incorrectos.";

    private final AdminUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final SessionTokens sessionTokens;
    private final Clock clock;

    /** Hash de referencia: un correo inexistente cuesta lo mismo que una contraseña equivocada. */
    private final String timingHash;

    public AuthServiceImpl(AdminUserRepository repository, PasswordEncoder passwordEncoder, SessionTokens sessionTokens,
                           Clock clock) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.sessionTokens = sessionTokens;
        this.clock = clock;
        this.timingHash = passwordEncoder.encode("referencia-para-igualar-tiempos");
    }

    @Override
    @Transactional
    public AuthSession login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String password = request.password();
        Optional<AdminUser> found = repository.findByEmail(email).filter(AdminUser::isActive);
        boolean valid = fitsBcrypt(password)
                && passwordEncoder.matches(password, found.map(AdminUser::getPasswordHash).orElse(timingHash))
                && found.isPresent();
        if (!valid) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, INVALID_CREDENTIALS);
        }
        AdminUser admin = found.get();
        admin.recordLogin(Instant.now(clock));
        return new AuthSession(toResponse(admin), sessionTokens.issueCookie(admin));
    }

    @Override
    @Transactional
    public void logout(Long adminId) {
        repository.findById(adminId).ifPresent(AdminUser::revokeSessions);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse me(Long adminId) {
        return toResponse(find(adminId));
    }

    @Override
    @Transactional
    public AuthSession changePassword(Long adminId, ChangePasswordRequest request) {
        AdminUser admin = find(adminId);
        if (!fitsBcrypt(request.currentPassword())
                || !passwordEncoder.matches(request.currentPassword(), admin.getPasswordHash())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "La contraseña actual no es correcta.");
        }
        if (!fitsBcrypt(request.newPassword())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "La contraseña nueva debe tener entre 12 y 72 caracteres.");
        }
        if (request.newPassword().equals(request.currentPassword())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "La contraseña nueva debe ser distinta de la actual.");
        }
        admin.changePassword(passwordEncoder.encode(request.newPassword()));
        // Flush para que la versión nueva quede en la base antes de emitir el token que la lleva.
        repository.flush();
        return new AuthSession(toResponse(admin), sessionTokens.issueCookie(admin));
    }

    private static boolean fitsBcrypt(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length <= MAX_PASSWORD_BYTES;
    }

    private AdminUser find(Long adminId) {
        return repository.findById(adminId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Tu sesión expiró, vuelve a ingresar."));
    }

    private static AdminUserResponse toResponse(AdminUser admin) {
        return new AdminUserResponse(admin.getId(), admin.getEmail(), admin.getRole(), admin.isMustChangePassword(),
                admin.getLastLoginAt());
    }
}
