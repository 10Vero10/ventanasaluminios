package com.ventanasyaluminios.dto.response;

import com.ventanasyaluminios.entity.enums.QuoteStatus;
import java.time.Instant;

/** Un cambio de estado de la cotización, para mostrar su historia. */
public record QuoteStatusChangeResponse(QuoteStatus fromStatus, QuoteStatus toStatus, Instant changedAt) {
}
