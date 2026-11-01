package com.ventanasyaluminios.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Fila de la ficha técnica de un producto. */
public record ProductSpecRequest(
        @NotBlank(message = "Cada fila de la ficha técnica necesita una etiqueta.")
        @Size(max = 60, message = "La etiqueta de la ficha técnica admite máximo 60 caracteres.")
        String label,

        @NotBlank(message = "Cada fila de la ficha técnica necesita un valor.")
        @Size(max = 150, message = "El valor de la ficha técnica admite máximo 150 caracteres.")
        String value) {
}
