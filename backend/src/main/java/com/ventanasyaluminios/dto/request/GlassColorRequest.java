package com.ventanasyaluminios.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Color de vidrio. El código se fija al crear y no cambia, porque las cotizaciones lo guardan. */
public record GlassColorRequest(
        @NotBlank(message = "El código es obligatorio.")
        @Pattern(regexp = RequestPatterns.OPTION_CODE,
                message = "El código solo puede tener mayúsculas, números y guion bajo (ej. AZUL_LAGO).")
        String code,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 60, message = "El nombre admite máximo 60 caracteres.")
        String name,

        @NotBlank(message = "El color de referencia es obligatorio.")
        @Pattern(regexp = RequestPatterns.HEX_COLOR, message = "El color debe estar en formato #RRGGBB (ej. #396DC6).")
        String hex,

        @NotNull(message = "Indica si el color es reflectivo.")
        Boolean reflective,

        @Size(max = 200, message = "La nota admite máximo 200 caracteres.")
        String note,

        @NotNull(message = "Indica si la opción está activa.")
        Boolean active,

        @NotNull(message = "Indica si es la opción por defecto.")
        Boolean defaultOption) {
}
