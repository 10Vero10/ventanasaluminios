package com.ventanasyaluminios.controller;

import com.ventanasyaluminios.dto.request.QuoteStatusRequest;
import com.ventanasyaluminios.dto.response.PageResponse;
import com.ventanasyaluminios.dto.response.QuoteDetailResponse;
import com.ventanasyaluminios.dto.response.QuoteResponse;
import com.ventanasyaluminios.entity.enums.QuoteStatus;
import com.ventanasyaluminios.exception.ApiException;
import com.ventanasyaluminios.exception.ErrorCode;
import com.ventanasyaluminios.security.AuthenticatedAdmin;
import com.ventanasyaluminios.service.QuoteService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Bandeja de cotizaciones del panel (rol ADMIN). */
@RestController
@RequestMapping("/api/v1/admin/quotes")
public class AdminQuoteController {

    static final String SORT_ASC = "createdAt,asc";
    static final String SORT_DESC = "createdAt,desc";

    private final QuoteService service;

    public AdminQuoteController(QuoteService service) {
        this.service = service;
    }

    /** Por defecto, de la más antigua a la más reciente (briefing §3.7). */
    @GetMapping
    public PageResponse<QuoteResponse> list(@RequestParam(required = false) QuoteStatus status,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(defaultValue = SORT_ASC) String sort) {
        if (!SORT_ASC.equals(sort) && !SORT_DESC.equals(sort)) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER,
                    "El orden debe ser " + SORT_ASC + " o " + SORT_DESC + ".");
        }
        return service.list(status, page, size, SORT_ASC.equals(sort));
    }

    @GetMapping("/{id}")
    public QuoteDetailResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PatchMapping("/{id}/status")
    public QuoteResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody QuoteStatusRequest request,
                                      @AuthenticationPrincipal AuthenticatedAdmin admin) {
        return service.changeStatus(id, request.status(), admin.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
