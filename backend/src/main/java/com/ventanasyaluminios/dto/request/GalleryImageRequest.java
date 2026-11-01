package com.ventanasyaluminios.dto.request;

import com.ventanasyaluminios.entity.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Imagen de la galería de trabajos. Sin título se guarda como "Imagen"; sin línea solo aparece en "Todos".
 */
public record GalleryImageRequest(
        @Size(max = 120, message = "El título admite máximo 120 caracteres.")
        String title,

        @NotBlank(message = "La URL de la imagen es obligatoria.")
        @Size(max = 2048, message = "La URL de la imagen es demasiado larga.")
        @Pattern(regexp = RequestPatterns.IMAGE_URL, message = "La imagen debe ser una URL http(s) válida.")
        String url,

        ProductCategory category) {
}
