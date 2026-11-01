package com.ventanasyaluminios.dto.request;

import com.ventanasyaluminios.entity.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Creación o edición de una línea destacada de la portada. */
public record FeaturedLineRequest(
        @NotBlank(message = "El número de la línea es obligatorio.")
        @Size(max = 4, message = "El número de la línea admite máximo 4 caracteres.")
        String number,

        @NotBlank(message = "El título es obligatorio.")
        @Size(max = 80, message = "El título admite máximo 80 caracteres.")
        String title,

        @NotBlank(message = "La etiqueta de categoría es obligatoria.")
        @Size(max = 80, message = "La etiqueta de categoría admite máximo 80 caracteres.")
        String categoryTag,

        @NotBlank(message = "La descripción es obligatoria.")
        @Size(max = 400, message = "La descripción admite máximo 400 caracteres.")
        String description,

        @Size(max = 2048, message = "La URL de la imagen es demasiado larga.")
        @Pattern(regexp = RequestPatterns.IMAGE_URL, message = "La imagen debe ser una URL http(s) válida.")
        String imageUrl,

        @NotBlank(message = "El texto del enlace es obligatorio.")
        @Size(max = 40, message = "El texto del enlace admite máximo 40 caracteres.")
        String linkText,

        @NotNull(message = "La categoría destino es obligatoria.")
        ProductCategory targetCategory) {
}
