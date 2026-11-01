package com.ventanasyaluminios.exception;

/** El recurso pedido no existe (404). */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
}
