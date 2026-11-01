package com.ventanasyaluminios.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Espesor de vidrio. Los milímetros se fijan al crear y no cambian, porque las cotizaciones los guardan. */
public record GlassThicknessRequest(
        @NotNull(message = "El espesor es obligatorio.")
        @Min(value = 1, message = "El espesor debe estar entre 1 y 50 mm.")
        @Max(value = 50, message = "El espesor debe estar entre 1 y 50 mm.")
        Integer mm,

        @NotNull(message = "Indica si la opción está activa.")
        Boolean active,

        @NotNull(message = "Indica si es la opción por defecto.")
        Boolean defaultOption) {
}
