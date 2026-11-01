package com.ventanasyaluminios.exception;

import org.springframework.http.HttpStatus;

/** La solicitud viola una regla de negocio que no cubre Bean Validation (400). */
public class InvalidRequestException extends ApiException {

    public InvalidRequestException(String code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }
}
