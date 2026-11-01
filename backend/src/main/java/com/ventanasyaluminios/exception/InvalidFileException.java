package com.ventanasyaluminios.exception;

import org.springframework.http.HttpStatus;

/** El archivo subido no es una imagen permitida o supera el tamaño máximo (400). */
public class InvalidFileException extends ApiException {

    public InvalidFileException(String code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }
}
