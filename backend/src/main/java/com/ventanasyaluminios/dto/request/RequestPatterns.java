package com.ventanasyaluminios.dto.request;

/** Expresiones regulares compartidas por las validaciones de los DTO de entrada. */
public final class RequestPatterns {

    /**
     * URL de imagen: vacía, http(s) externa o ruta del mismo sitio ({@code /media/...}). Rechaza
     * {@code data:} (base64 en la base) y {@code javascript:}.
     */
    public static final String IMAGE_URL = "^$|^https?://\\S+$|^/(?!/)\\S*$";

    /** Teléfono: dígitos, espacios, +, guiones y paréntesis. */
    public static final String PHONE = "^[0-9+()\\-\\s]*$";

    /** Número de WhatsApp: vacío o solo dígitos tras normalizar; aquí se aceptan también espacios y +. */
    public static final String WHATSAPP = "^[0-9+\\s]*$";

    private RequestPatterns() {
    }
}
