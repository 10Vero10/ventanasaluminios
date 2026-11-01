package com.ventanasyaluminios.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cambio de contraseña del administrador; obligatorio en el primer ingreso. */
public record ChangePasswordRequest(
        @NotBlank(message = "Ingresa tu contraseña actual.")
        @Size(max = 200, message = "La contraseña actual no es correcta.")
        String currentPassword,

        @NotBlank(message = "Ingresa la contraseña nueva.")
        @Size(min = 12, max = 72, message = "La contraseña nueva debe tener entre 12 y 72 caracteres.")
        String newPassword) {
}
