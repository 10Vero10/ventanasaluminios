package com.ventanasyaluminios.exception;

import org.springframework.http.HttpStatus;

/**
 * Base de las excepciones de negocio. Los services las lanzan y {@link GlobalExceptionHandler}
 * las convierte en {@code { "error", "code" }} con el estado HTTP indicado.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    protected ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
