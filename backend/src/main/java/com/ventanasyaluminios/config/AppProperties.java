package com.ventanasyaluminios.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

/**
 * Configuración propia de la aplicación (prefijo {@code app}). Se valida al arrancar: si algo falta o es
 * inválido, la aplicación no inicia.
 *
 * @param cors      orígenes permitidos para CORS
 * @param media     almacenamiento de imágenes subidas
 * @param rateLimit límites de solicitudes por IP
 * @param auth      sesión del panel y administrador inicial
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(@Valid @NotNull Cors cors, @Valid @NotNull Media media, @Valid @NotNull RateLimit rateLimit,
                            @Valid @NotNull Auth auth) {

    /**
     * @param allowedOrigins orígenes con acceso CORS. Vacío en producción, porque frontend y API
     *                       comparten dominio detrás de nginx.
     */
    public record Cors(@NotNull List<String> allowedOrigins) {

        /** Con cookies de sesión, CORS no admite "*": Spring fallaría en cada solicitud. */
        @AssertTrue(message = "app.cors.allowed-origins no puede contener '*': lista los orígenes exactos.")
        public boolean isWithoutWildcard() {
            return allowedOrigins == null || allowedOrigins.stream().noneMatch(origin -> origin.contains("*"));
        }
    }

    /**
     * @param uploadDir   carpeta donde se guardan las imágenes (en Docker, un volumen)
     * @param maxFileSize tamaño máximo por imagen
     */
    public record Media(@NotBlank String uploadDir, @NotNull DataSize maxFileSize) {

        /** Ruta pública bajo la que se sirven las imágenes subidas. */
        public static final String PUBLIC_PREFIX = "/media/";

        /** Carpeta de imágenes como ruta absoluta; la usan quien guarda y quien sirve los archivos. */
        public Path uploadPath() {
            return Path.of(uploadDir).toAbsolutePath().normalize();
        }

        /** Límite legible para los mensajes de error, por ejemplo "5 MB". */
        public String maxFileSizeLabel() {
            long megabytes = maxFileSize.toMegabytes();
            return megabytes >= 1 ? megabytes + " MB" : maxFileSize.toKilobytes() + " KB";
        }
    }

    /**
     * @param quotes envíos del formulario de cotización (briefing §3.6: 10 por hora)
     * @param login  intentos de inicio de sesión (5 por minuto)
     */
    public record RateLimit(@Valid @NotNull Rule quotes, @Valid @NotNull Rule login) {
    }

    /**
     * @param jwtSecret            clave HS256 de los JWT de sesión (variable JWT_SECRET, mínimo 32 caracteres)
     * @param sessionDuration      duración de una sesión del panel
     * @param cookieSecure         marca la cookie como Secure (solo HTTPS); {@code localhost} cuenta como seguro
     * @param initialAdminEmail    correo del primer administrador, si todavía no existe ninguno
     * @param initialAdminPassword contraseña temporal del primer administrador; se debe cambiar en el primer ingreso
     */
    public record Auth(@NotBlank(message = "Falta JWT_SECRET: la clave para firmar las sesiones del panel.")
                       @Size(min = 32, message = "JWT_SECRET debe tener al menos 32 caracteres.") String jwtSecret,
                       @NotNull Duration sessionDuration,
                       boolean cookieSecure,
                       String initialAdminEmail,
                       String initialAdminPassword) {

        /** El administrador inicial necesita correo y contraseña juntos, o ninguno de los dos. */
        @AssertTrue(message = "ADMIN_EMAIL y ADMIN_INITIAL_PASSWORD van juntos; la contraseña debe tener al menos 12 caracteres.")
        public boolean isInitialAdminConsistent() {
            boolean hasEmail = initialAdminEmail != null && !initialAdminEmail.isBlank();
            boolean hasPassword = initialAdminPassword != null && !initialAdminPassword.isBlank();
            if (hasEmail != hasPassword) {
                return false;
            }
            return !hasPassword || initialAdminPassword.length() >= 12;
        }
    }

    /**
     * @param maxRequests solicitudes permitidas por IP dentro de la ventana
     * @param window      duración de la ventana
     */
    public record Rule(@Positive int maxRequests, @NotNull Duration window) {
    }
}
