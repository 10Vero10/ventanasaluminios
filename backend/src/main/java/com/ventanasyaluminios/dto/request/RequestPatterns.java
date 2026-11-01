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

    /** Código de una opción de vidrio: mayúsculas, números y guion bajo (ej. AZUL_LAGO). */
    public static final String OPTION_CODE = "^[A-Z0-9_]{2,30}$";

    /** Color en hexadecimal de seis dígitos (ej. #396DC6). */
    public static final String HEX_COLOR = "^#[0-9A-Fa-f]{6}$";

    private RequestPatterns() {
    }
}
