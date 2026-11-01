package com.ventanasyaluminios.dto.response;

/** Color de vidrio del configurador; {@code note} explica variantes como el acabado reflectivo. */
public record GlassColorResponse(Long id, int position, String code, String name, String hex, boolean reflective,
                                 String note, boolean active, boolean defaultOption) {
}
