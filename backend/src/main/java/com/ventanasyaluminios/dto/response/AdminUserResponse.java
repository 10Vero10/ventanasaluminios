package com.ventanasyaluminios.dto.response;

import com.ventanasyaluminios.entity.enums.AdminRole;
import java.time.Instant;

/** Administrador de la sesión actual. Nunca incluye el hash de la contraseña. */
public record AdminUserResponse(Long id, String email, AdminRole role, boolean mustChangePassword, Instant lastLoginAt) {
}
