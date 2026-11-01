package com.ventanasyaluminios.dto.response;

import java.util.List;

/** Las tres listas del configurador de vidrio en una sola respuesta, cada una en orden. */
public record GlassOptionsResponse(
        List<GlassTypeResponse> types,
        List<GlassColorResponse> colors,
        List<GlassThicknessResponse> thicknesses) {
}
