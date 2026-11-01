package com.ventanasyaluminios.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(@Valid @NotNull Cors cors, @Valid @NotNull Media media, @Valid @NotNull RateLimit rateLimit) {

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

    /** @param quotes envíos del formulario de cotización (briefing §3.6: 10 por hora) */
    public record RateLimit(@Valid @NotNull Rule quotes) {
    }

    /**
     * @param maxRequests solicitudes permitidas por IP dentro de la ventana
     * @param window      duración de la ventana
     */
    public record Rule(@Positive int maxRequests, @NotNull Duration window) {
    }
}
