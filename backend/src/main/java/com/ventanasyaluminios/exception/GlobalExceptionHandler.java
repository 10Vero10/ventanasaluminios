package com.ventanasyaluminios.exception;

import com.ventanasyaluminios.config.AppProperties;
import com.ventanasyaluminios.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduce toda excepción a {@link ErrorResponse} con mensaje en español. Hereda de
 * {@link ResponseEntityExceptionHandler} para que los errores propios de Spring MVC conserven su estado
 * (400, 404, 405, 406, 413, 415…) en lugar de terminar como 500. Ningún detalle técnico llega al cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final AppProperties properties;

    public GlobalExceptionHandler(AppProperties properties) {
        this.properties = properties;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
        return build(ex.getCode(), ex.getMessage());
    }

    /** Dos cambios simultáneos chocaron con una restricción de la base (por ejemplo, la misma posición). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Conflicto de integridad: {}", ex.getMostSpecificCause().getMessage());
        return build(ErrorCode.CONFLICT, "Otro cambio se guardó al mismo tiempo. Actualiza la página e intenta de nuevo.");
    }

    /** Dos ediciones cruzadas del mismo registro: la segunda llegó con datos viejos o el registro ya no existe. */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex) {
        log.warn("Conflicto de edición: {}", ex.getMessage());
        return build(ErrorCode.CONFLICT, "Otro cambio se guardó al mismo tiempo. Actualiza la página e intenta de nuevo.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Error inesperado", ex);
        return build(ErrorCode.INTERNAL_ERROR, "Ocurrió un error inesperado. Intenta de nuevo.");
    }

    /** Punto único por el que pasan todas las excepciones de Spring MVC que maneja la clase base. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        if (statusCode.is5xxServerError()) {
            log.error("Error del servidor en Spring MVC", ex);
        } else {
            log.debug("Solicitud rechazada ({}): {}", statusCode.value(), ex.getMessage());
        }
        // Si el cliente no acepta JSON, no hay formato en el que responder: solo el estado.
        if (statusCode.value() == HttpStatus.NOT_ACCEPTABLE.value()) {
            return ResponseEntity.status(statusCode).headers(headers).build();
        }
        ErrorResponse error = describe(ex, statusCode);
        return ResponseEntity.status(statusCode).headers(headers).body(error);
    }

    private ErrorResponse describe(Exception ex, HttpStatusCode statusCode) {
        if (ex instanceof MethodArgumentNotValidException invalid) {
            String message = invalid.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .findFirst()
                    .orElse("Los datos enviados no son válidos.");
            return new ErrorResponse(message, ErrorCode.VALIDATION_ERROR.name());
        }
        if (ex instanceof HandlerMethodValidationException invalid) {
            String message = invalid.getAllErrors().stream()
                    .map(MessageSourceResolvable::getDefaultMessage)
                    .findFirst()
                    .orElse("Los datos enviados no son válidos.");
            return new ErrorResponse(message, ErrorCode.VALIDATION_ERROR.name());
        }
        if (ex instanceof HttpMessageNotReadableException) {
            return error(ErrorCode.MALFORMED_REQUEST, "El contenido de la solicitud no es válido.");
        }
        if (ex instanceof TypeMismatchException || ex instanceof ServletRequestBindingException
                || ex instanceof MissingServletRequestPartException) {
            return error(ErrorCode.INVALID_PARAMETER, "Falta un dato o tiene un formato no válido.");
        }
        if (ex instanceof MaxUploadSizeExceededException) {
            return error(ErrorCode.FILE_TOO_LARGE, "La imagen es demasiado pesada. El máximo es "
                    + properties.media().maxFileSizeLabel() + ".");
        }
        if (ex instanceof NoResourceFoundException) {
            return error(ErrorCode.NOT_FOUND, "El recurso solicitado no existe.");
        }
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return error(ErrorCode.METHOD_NOT_ALLOWED, "Operación no permitida en esta ruta.");
        }
        if (ex instanceof HttpMediaTypeNotSupportedException) {
            return error(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Formato de contenido no soportado.");
        }
        if (statusCode.is5xxServerError()) {
            return error(ErrorCode.INTERNAL_ERROR, "Ocurrió un error inesperado. Intenta de nuevo.");
        }
        return error(ErrorCode.BAD_REQUEST, "La solicitud no es válida.");
    }

    private static ErrorResponse error(ErrorCode code, String message) {
        return new ErrorResponse(message, code.name());
    }

    private static ResponseEntity<ErrorResponse> build(ErrorCode code, String message) {
        return ResponseEntity.status(code.getStatus()).body(error(code, message));
    }
}
