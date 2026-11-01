package com.ventanasyaluminios.dto.request;

import com.ventanasyaluminios.entity.enums.ProductCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Creación o edición de un producto. La ficha técnica y las características llegan completas y en el orden en
 * que se muestran: reemplazan a las anteriores.
 */
public record ProductRequest(
        @NotNull(message = "La categoría es obligatoria.")
        ProductCategory category,

        @Size(max = 4, message = "El número de categoría admite máximo 4 caracteres.")
        String categoryNumber,

        @NotBlank(message = "El título es obligatorio.")
        @Size(max = ProductRequest.MAX_TITLE_LENGTH, message = "El título admite máximo 80 caracteres.")
        String title,

        @NotBlank(message = "El subtítulo es obligatorio.")
        @Size(max = 120, message = "El subtítulo admite máximo 120 caracteres.")
        String subtitle,

        @NotBlank(message = "La descripción es obligatoria.")
        @Size(max = 600, message = "La descripción admite máximo 600 caracteres.")
        String description,

        @Size(max = 2048, message = "La URL de la imagen es demasiado larga.")
        @Pattern(regexp = RequestPatterns.IMAGE_URL, message = "La imagen debe ser una URL http(s) válida.")
        String imageUrl,

        @Size(max = 60, message = "El distintivo admite máximo 60 caracteres.")
        String badge,

        @Size(max = 60, message = "El texto del botón admite máximo 60 caracteres.")
        String ctaText,

        @NotNull(message = "Indica si el producto está visible en el catálogo.")
        Boolean active,

        @NotNull(message = "La ficha técnica es obligatoria (puede ser una lista vacía).")
        @Size(max = 12, message = "La ficha técnica admite máximo 12 filas.")
        List<@Valid @NotNull(message = "La ficha técnica tiene una fila vacía.") ProductSpecRequest> specs,

        @NotNull(message = "Las características son obligatorias (puede ser una lista vacía).")
        @Size(max = 12, message = "Se admiten máximo 12 características.")
        List<@NotBlank(message = "Hay una característica vacía.")
            @Size(max = 150, message = "Cada característica admite máximo 150 caracteres.") String> features) {

    /** Largo máximo del título (columna product.title); también limita el título de una copia. */
    public static final int MAX_TITLE_LENGTH = 80;
}
