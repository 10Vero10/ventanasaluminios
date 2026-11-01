package com.ventanasyaluminios.mapper;

/** Limpieza de textos que llegan del panel, común a todos los mappers. */
final class TextNormalization {

    private TextNormalization() {
    }

    /** Recorta espacios; vacío o solo espacios se guarda como {@code null}. */
    static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /** Recorta espacios; si queda vacío, usa {@code defaultValue}. */
    static String trimOrDefault(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    /** Deja solo los dígitos ("+57 317 846 3260" → "573178463260"); {@code null} queda vacío. */
    static String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
