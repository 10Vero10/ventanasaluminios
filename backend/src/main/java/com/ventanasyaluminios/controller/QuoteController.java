package com.ventanasyaluminios.controller;

import com.ventanasyaluminios.dto.request.QuoteCreateRequest;
import com.ventanasyaluminios.dto.response.QuoteCreatedResponse;
import com.ventanasyaluminios.service.QuoteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Formulario público de cotización. Sin {@code @Valid}: el service revisa primero el campo trampa y luego valida.
 * El límite de envíos por IP lo aplica {@code RateLimitFilter} antes de llegar aquí.
 */
@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {

    private final QuoteService service;

    public QuoteController(QuoteService service) {
        this.service = service;
    }

    /** {@code getRemoteAddr} ya trae la IP real: Tomcat la toma de X-Forwarded-For solo si viene de un proxy interno. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteCreatedResponse create(@RequestBody QuoteCreateRequest request, HttpServletRequest http) {
        return service.create(request, http.getRemoteAddr());
    }
}
