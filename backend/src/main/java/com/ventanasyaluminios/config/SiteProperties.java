package com.ventanasyaluminios.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Carpeta con el frontend compilado ({@code SITE_DIR}). Vacía, el backend solo atiende la API (Docker Compose, donde
 * nginx sirve el sitio); con valor, el backend también sirve el sitio (Render, un solo servicio: ADR-022).
 */
@ConfigurationProperties(prefix = "app.site")
public record SiteProperties(@DefaultValue("") String dir) {

    public boolean enabled() {
        return dir != null && !dir.isBlank();
    }
}
