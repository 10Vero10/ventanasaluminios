package com.ventanasyaluminios.exception;

import org.springframework.http.HttpStatus;

/** El recurso pedido no existe (404). */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
    }
}
