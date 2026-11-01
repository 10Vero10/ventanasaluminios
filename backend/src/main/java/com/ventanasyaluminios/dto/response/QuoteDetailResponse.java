package com.ventanasyaluminios.dto.response;

import java.util.List;

/** Detalle de una cotización con su historial de estados, del más antiguo al más reciente. */
public record QuoteDetailResponse(QuoteResponse quote, List<QuoteStatusChangeResponse> history) {
}
