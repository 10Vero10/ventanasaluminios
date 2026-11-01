package com.ventanasyaluminios.exception;

/**
 * Error de negocio que lanzan los services. {@link GlobalExceptionHandler} lo convierte en
 * {@code { "error", "code" }} con el estado HTTP del {@link ErrorCode}.
 */
public class ApiException extends RuntimeException {

    private final ErrorCode code;

    /**
     * @param code    código y estado HTTP del error
     * @param message mensaje en español, listo para mostrar al usuario
     */
    public ApiException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
