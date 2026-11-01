package com.ventanasyaluminios.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

/**
 * Configuración propia de la aplicación (prefijo {@code app}). Se valida al arrancar: si falta algo,
 * la aplicación no inicia.
 *
 * @param cors  orígenes permitidos para CORS
 * @param media almacenamiento de imágenes subidas
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(@Valid @NotNull Cors cors, @Valid @NotNull Media media) {

    /**
     * @param allowedOrigins orígenes con acceso CORS. Vacío en producción, porque frontend y API
     *                       comparten dominio detrás de nginx.
     */
    public record Cors(@NotNull List<String> allowedOrigins) {
    }

    /**
     * @param uploadDir   carpeta donde se guardan las imágenes (en Docker, un volumen)
     * @param maxFileSize tamaño máximo por imagen
     */
    public record Media(@NotBlank String uploadDir, @NotNull DataSize maxFileSize) {
    }
}
