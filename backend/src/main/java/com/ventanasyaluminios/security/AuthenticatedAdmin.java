package com.ventanasyaluminios.security;

/**
 * Administrador de la solicitud actual, ya verificado contra la base: activo y con la versión de token vigente.
 *
 * @param mustChangePassword si todavía debe cambiar la contraseña temporal; mientras tanto no accede al panel
 */
public record AuthenticatedAdmin(Long id, String email, boolean mustChangePassword) {
}
