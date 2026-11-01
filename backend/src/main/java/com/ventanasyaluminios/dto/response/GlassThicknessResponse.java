package com.ventanasyaluminios.dto.response;

/** Espesor de vidrio del configurador. */
public record GlassThicknessResponse(Long id, int position, int mm, boolean active, boolean defaultOption) {
}
