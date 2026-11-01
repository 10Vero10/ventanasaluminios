package com.ventanasyaluminios.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Tipo de vidrio. El código se fija al crear y no cambia, porque las cotizaciones lo guardan. */
public record GlassTypeRequest(
        @NotBlank(message = "El código es obligatorio.")
        @Pattern(regexp = RequestPatterns.OPTION_CODE,
                message = "El código solo puede tener mayúsculas, números y guion bajo (ej. LAMINADO_TEMPLADO).")
        String code,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 60, message = "El nombre admite máximo 60 caracteres.")
        String name,

        @NotNull(message = "Indica si la opción está activa.")
        Boolean active,

        @NotNull(message = "Indica si es la opción por defecto.")
        Boolean defaultOption) {
}
