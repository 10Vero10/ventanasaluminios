package com.ventanasyaluminios.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Inicio de sesión del panel con correo y contraseña. */
public record LoginRequest(
        @NotBlank(message = "Ingresa tu correo y tu contraseña.")
        @Size(max = 254, message = "Correo o contraseña incorrectos.")
        String email,

        @NotBlank(message = "Ingresa tu correo y tu contraseña.")
        @Size(max = 200, message = "Correo o contraseña incorrectos.")
        String password) {
}
