package com.ventanasyaluminios.service;

import com.ventanasyaluminios.dto.request.QuoteCreateRequest;
import com.ventanasyaluminios.dto.response.PageResponse;
import com.ventanasyaluminios.dto.response.QuoteCreatedResponse;
import com.ventanasyaluminios.dto.response.QuoteDetailResponse;
import com.ventanasyaluminios.dto.response.QuoteResponse;
import com.ventanasyaluminios.entity.enums.QuoteStatus;
import java.util.UUID;

/** Cotizaciones: la solicitud pública y la bandeja del panel. */
public interface QuoteService {

    /**
     * Recibe una solicitud del formulario público (briefing §3.6). Si el campo trampa viene lleno responde como si se
     * hubiera guardado, sin guardar nada.
     */
    QuoteCreatedResponse create(QuoteCreateRequest request, String sourceIp);

    /**
     * Bandeja paginada.
     *
     * @param status    filtro opcional por estado
     * @param page      página desde 0
     * @param size      tamaño de página (1 a 100)
     * @param ascending {@code true}: de la más antigua a la más reciente (por defecto)
     */
    PageResponse<QuoteResponse> list(QuoteStatus status, int page, int size, boolean ascending);

    QuoteDetailResponse get(UUID id);

    /** Cambia el estado libremente (ADR-006) y deja el cambio en el historial con el administrador que lo hizo. */
    QuoteResponse changeStatus(UUID id, QuoteStatus status, Long changedBy);

    /** Borrado lógico: la cotización desaparece de la bandeja pero su fila queda. */
    void delete(UUID id);
}
