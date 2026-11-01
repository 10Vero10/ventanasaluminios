package com.ventanasyaluminios.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ventanasyaluminios.dto.response.ErrorResponse;
import com.ventanasyaluminios.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;

/**
 * Respuestas 401 y 403 de Spring Security en el formato de error de la API ({@code { "error", "code" }}), con mensajes
 * en español.
 */
public final class SecurityErrorResponses {

    private SecurityErrorResponses() {
    }

    /** 401: no hay sesión válida (sin cookie, vencida, revocada o alterada). */
    public static AuthenticationEntryPoint entryPoint(ObjectMapper objectMapper) {
        return (request, response, exception) -> write(response, objectMapper, ErrorCode.UNAUTHORIZED,
                "Tu sesión expiró, vuelve a ingresar.");
    }

    /** 403: token CSRF ausente o inválido, contraseña temporal sin cambiar, o falta de permiso. */
    public static AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, exception) -> {
            if (exception instanceof CsrfException) {
                write(response, objectMapper, ErrorCode.CSRF_INVALID,
                        "La verificación de seguridad expiró. Actualiza la página e intenta de nuevo.");
            } else if (mustChangePassword()) {
                write(response, objectMapper, ErrorCode.PASSWORD_CHANGE_REQUIRED,
                        "Debes cambiar tu contraseña temporal antes de usar el panel.");
            } else {
                write(response, objectMapper, ErrorCode.FORBIDDEN, "No tienes permiso para esta acción.");
            }
        };
    }

    private static boolean mustChangePassword() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof AuthenticatedAdmin admin
                && admin.mustChangePassword();
    }

    private static void write(HttpServletResponse response, ObjectMapper objectMapper, ErrorCode code, String message)
            throws IOException {
        response.setStatus(code.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new ErrorResponse(message, code.name()));
    }
}
