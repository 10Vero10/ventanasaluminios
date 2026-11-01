package com.ventanasyaluminios.dto.response;

/** Tipo de vidrio del configurador. */
public record GlassTypeResponse(Long id, int position, String code, String name, boolean active,
                                boolean defaultOption) {
}
