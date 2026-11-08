package com.ventanasyaluminios.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;
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
 * @param site      frontend compilado que sirve el backend
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(@Valid @NotNull Cors cors, @Valid @NotNull Media media, @Valid @NotNull RateLimit rateLimit,
                            @Valid @NotNull Auth auth, @Valid @NotNull Site site) {

    /**
     * @param allowedOrigins orígenes con acceso CORS. Vacío en producción, porque el backend sirve el frontend y
     *                       comparten dominio.
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
     * @param dir carpeta del frontend compilado (variable SITE_DIR, la define el Dockerfile). Vacía, el backend solo
     *            atiende la API (pruebas, o al correrlo desde el IDE con Vite aparte).
     */
    public record Site(String dir) {

        public boolean enabled() {
            return dir != null && !dir.isBlank();
        }

        public Path path() {
            return Path.of(dir).toAbsolutePath().normalize();
        }

        /** Una carpeta equivocada arrancaría "sana" pero sin sitio: mejor no arrancar. */
        @AssertTrue(message = "SITE_DIR no contiene index.html: debe apuntar al frontend compilado o quedar vacía.")
        public boolean isBuilt() {
            return !enabled() || Files.isRegularFile(path().resolve("index.html"));
        }
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

        /**
         * Valores de ejemplo de {@code .env.example}. El repositorio es público: con ellos cualquiera podría firmar
         * sesiones del panel o entrar primero como administrador. Si llegan sin cambiar, el backend no arranca.
         */
        static final Set<String> EXAMPLE_VALUES = Set.of(
                "cambiar-por-una-clave-larga-y-aleatoria-de-al-menos-32-caracteres",
                "cambiar-esta-clave-temporal");

        @AssertTrue(message = "JWT_SECRET sigue con el valor de ejemplo de .env.example: genera una clave propia.")
        public boolean isJwtSecretNotExample() {
            return jwtSecret == null || !EXAMPLE_VALUES.contains(jwtSecret.trim());
        }

        @AssertTrue(message = "ADMIN_INITIAL_PASSWORD sigue con el valor de ejemplo de .env.example: elige una contraseña propia.")
        public boolean isInitialAdminPasswordNotExample() {
            return initialAdminPassword == null || !EXAMPLE_VALUES.contains(initialAdminPassword.trim());
        }

        /**
         * Una contraseña inicial necesita su correo y al menos 12 caracteres. El correo solo, sin contraseña, es válido:
         * después del primer ingreso se puede borrar ADMIN_INITIAL_PASSWORD del .env.
         */
        @AssertTrue(message = "ADMIN_INITIAL_PASSWORD necesita ADMIN_EMAIL y al menos 12 caracteres.")
        public boolean isInitialAdminConsistent() {
            boolean hasEmail = initialAdminEmail != null && !initialAdminEmail.isBlank();
            boolean hasPassword = initialAdminPassword != null && !initialAdminPassword.isBlank();
            return !hasPassword || (hasEmail && initialAdminPassword.length() >= 12);
        }
    }

    /**
     * @param maxRequests solicitudes permitidas por IP dentro de la ventana
     * @param window      duración de la ventana
     */
    public record Rule(@Positive int maxRequests, @NotNull Duration window) {
    }
}
