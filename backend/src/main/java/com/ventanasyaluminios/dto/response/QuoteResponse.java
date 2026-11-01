package com.ventanasyaluminios.dto.response;

import com.ventanasyaluminios.entity.enums.QuoteStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Cotización en la bandeja del panel. Los datos del configurador son nulos si el cliente no lo usó. */
public record QuoteResponse(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        QuoteStatus status,
        String name,
        String phone,
        String productType,
        String dimensionsText,
        String message,
        Integer widthCm,
        Integer heightCm,
        Integer panes,
        BigDecimal areaM2,
        String glassTypeCode,
        String glassColorCode,
        Integer thicknessMm) {
}
