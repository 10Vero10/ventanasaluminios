package com.ventanasyaluminios.dto.response;

/**
 * Formato único de error de la API.
 *
 * @param error mensaje en español, listo para mostrar al usuario
 * @param code  código estable para que el frontend distinga el caso
 */
public record ErrorResponse(String error, String code) {
}
