package com.ventanasyaluminios.service;

import com.ventanasyaluminios.dto.request.ChangePasswordRequest;
import com.ventanasyaluminios.dto.request.LoginRequest;
import com.ventanasyaluminios.dto.response.AdminUserResponse;

/** Sesión del panel: ingreso, salida, usuario actual y cambio de contraseña. */
public interface AuthService {

    /**
     * Sesión emitida: el administrador y el encabezado Set-Cookie con el JWT.
     *
     * @param sessionCookie valor completo del encabezado Set-Cookie
     */
    record AuthSession(AdminUserResponse admin, String sessionCookie) {
    }

    /** Verifica correo y contraseña. Ante cualquier fallo responde lo mismo, para no revelar qué correos existen. */
    AuthSession login(LoginRequest request);

    /** Cierra todas las sesiones de este administrador (sube su versión de token). */
    void logout(Long adminId);

    AdminUserResponse me(Long adminId);

    /** Cambia la contraseña, invalida las sesiones anteriores y emite una nueva. */
    AuthSession changePassword(Long adminId, ChangePasswordRequest request);
}
