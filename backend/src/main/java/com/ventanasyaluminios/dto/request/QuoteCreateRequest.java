package com.ventanasyaluminios.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud de cotización del formulario público (briefing §3.6). Los datos del configurador (medidas, hojas y
 * vidrio) son opcionales pero van todos juntos; el servidor recalcula el área y el texto de medidas. {@code website}
 * es un campo trampa oculto: si llega con contenido, la solicitud es de un bot.
 */
public record QuoteCreateRequest(
        @NotBlank(message = "Nombre y teléfono son obligatorios.")
        @Size(max = 100, message = "El nombre admite máximo 100 caracteres.")
        String name,

        @NotBlank(message = "Nombre y teléfono son obligatorios.")
        @Size(max = 40, message = "El teléfono admite máximo 40 caracteres.")
        String phone,

        @Size(max = 120, message = "El tipo de producto admite máximo 120 caracteres.")
        String productType,

        @Size(max = 200, message = "Las medidas admiten máximo 200 caracteres.")
        String dimensionsText,

        @Size(max = 2000, message = "El mensaje admite máximo 2000 caracteres.")
        String message,

        @Min(value = 1, message = "El ancho debe estar entre 1 y 600 cm.")
        @Max(value = 600, message = "El ancho debe estar entre 1 y 600 cm.")
        Integer widthCm,

        @Min(value = 1, message = "El alto debe estar entre 1 y 260 cm.")
        @Max(value = 260, message = "El alto debe estar entre 1 y 260 cm.")
        Integer heightCm,

        @Min(value = 1, message = "El número de hojas debe estar entre 1 y 4.")
        @Max(value = 4, message = "El número de hojas debe estar entre 1 y 4.")
        Integer panes,

        @Size(max = 30, message = "El tipo de vidrio no es válido.")
        String glassTypeCode,

        @Size(max = 30, message = "El color de vidrio no es válido.")
        String glassColorCode,

        Integer thicknessMm,

        String website) {

    /** Si el cliente usó el configurador (llegó al menos un dato de medidas o vidrio). */
    public boolean hasConfiguration() {
        return widthCm != null || heightCm != null || panes != null || glassTypeCode != null || glassColorCode != null
                || thicknessMm != null;
    }
}
