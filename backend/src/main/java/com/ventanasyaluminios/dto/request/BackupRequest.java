package com.ventanasyaluminios.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

/**
 * Respaldo del contenido del sitio (briefing §3.10): {@code { version, exportedAt, content, products }}. Usa los mismos
 * DTO que el panel, así exportar e importar pasan por las mismas validaciones. El orden de cada lista es el orden en
 * pantalla.
 */
public record BackupRequest(
        int version,
        Instant exportedAt,
        @Valid @NotNull Content content,
        @Valid @NotNull @Size(max = 500, message = "El respaldo tiene demasiados productos.")
        List<@Valid @NotNull ProductRequest> products) {

    /** Versión actual del formato. */
    public static final int CURRENT_VERSION = 1;

    /** Todo el contenido editable que no es el catálogo. */
    public record Content(
            @Valid @NotNull SiteSettingsRequest settings,
            @Valid @NotNull @Size(max = 50) List<@Valid @NotNull FeaturedLineRequest> featuredLines,
            @Valid @NotNull @Size(max = 1000) List<@Valid @NotNull GalleryImageRequest> gallery,
            @Valid @NotNull GlassOptions glassOptions) {
    }

    /** Opciones del configurador. Al importar se actualizan por código; las ausentes se desactivan, no se borran. */
    public record GlassOptions(
            @Valid @NotNull @Size(max = 100) List<@Valid @NotNull GlassTypeRequest> types,
            @Valid @NotNull @Size(max = 100) List<@Valid @NotNull GlassColorRequest> colors,
            @Valid @NotNull @Size(max = 100) List<@Valid @NotNull GlassThicknessRequest> thicknesses) {
    }
}
