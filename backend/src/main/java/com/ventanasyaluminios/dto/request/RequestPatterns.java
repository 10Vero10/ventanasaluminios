package com.ventanasyaluminios.dto.request;

/** Expresiones regulares compartidas por las validaciones de los DTO de entrada. */
public final class RequestPatterns {

    /**
     * URL de imagen: vacía, http(s) externa o ruta del mismo sitio ({@code /media/...}). Rechaza
     * {@code data:} (base64 en la base), {@code javascript:} y rutas que el navegador leería como otro
     * dominio ({@code //host}, {@code /\host}).
     */
    public static final String IMAGE_URL = "^$|^https?://\\S+$|^/(?![/\\\\])[^\\s\\\\]*$";

    /** Teléfono: al menos un dígito; además solo espacios, +, guiones y paréntesis. */
    public static final String PHONE = "^(?=.*\\d)[0-9+()\\-\\s]*$";

    /** WhatsApp tal como lo escribe el administrador: dígitos, espacios y +. Se guarda solo con dígitos. */
    public static final String WHATSAPP = "^[0-9+\\s]*$";

    private RequestPatterns() {
    }
}
