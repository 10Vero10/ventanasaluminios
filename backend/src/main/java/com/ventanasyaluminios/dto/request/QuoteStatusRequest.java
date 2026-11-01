package com.ventanasyaluminios.dto.request;

import com.ventanasyaluminios.entity.enums.QuoteStatus;
import jakarta.validation.constraints.NotNull;

/** Nuevo estado de una cotización en la bandeja. */
public record QuoteStatusRequest(@NotNull(message = "El estado es obligatorio.") QuoteStatus status) {
}
