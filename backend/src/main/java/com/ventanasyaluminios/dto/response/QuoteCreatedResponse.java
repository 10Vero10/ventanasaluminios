package com.ventanasyaluminios.dto.response;

import java.util.UUID;

/** Respuesta al visitante: solo el id de su cotización. */
public record QuoteCreatedResponse(UUID id) {
}
