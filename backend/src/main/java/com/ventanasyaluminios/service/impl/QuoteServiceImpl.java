package com.ventanasyaluminios.service.impl;

import com.ventanasyaluminios.dto.request.QuoteCreateRequest;
import com.ventanasyaluminios.dto.response.PageResponse;
import com.ventanasyaluminios.dto.response.QuoteCreatedResponse;
import com.ventanasyaluminios.dto.response.QuoteDetailResponse;
import com.ventanasyaluminios.dto.response.QuoteResponse;
import com.ventanasyaluminios.entity.GlassColor;
import com.ventanasyaluminios.entity.GlassThickness;
import com.ventanasyaluminios.entity.GlassType;
import com.ventanasyaluminios.entity.Quote;
import com.ventanasyaluminios.entity.QuoteStatusChange;
import com.ventanasyaluminios.entity.SiteSettings;
import com.ventanasyaluminios.entity.enums.QuoteStatus;
import com.ventanasyaluminios.exception.ApiException;
import com.ventanasyaluminios.exception.ErrorCode;
import com.ventanasyaluminios.exception.ResourceNotFoundException;
import com.ventanasyaluminios.mapper.QuoteMapper;
import com.ventanasyaluminios.repository.GlassColorRepository;
import com.ventanasyaluminios.repository.GlassThicknessRepository;
import com.ventanasyaluminios.repository.GlassTypeRepository;
import com.ventanasyaluminios.repository.QuoteRepository;
import com.ventanasyaluminios.repository.QuoteStatusChangeRepository;
import com.ventanasyaluminios.repository.SiteSettingsRepository;
import com.ventanasyaluminios.service.QuoteService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de {@link QuoteService}. */
@Service
public class QuoteServiceImpl implements QuoteService {

    static final int MAX_PAGE_SIZE = 100;

    private final QuoteRepository quoteRepository;
    private final QuoteStatusChangeRepository statusChangeRepository;
    private final SiteSettingsRepository settingsRepository;
    private final GlassTypeRepository glassTypeRepository;
    private final GlassColorRepository glassColorRepository;
    private final GlassThicknessRepository glassThicknessRepository;
    private final QuoteMapper mapper;
    private final Validator validator;
    private final Clock clock;

    public QuoteServiceImpl(QuoteRepository quoteRepository,
                            QuoteStatusChangeRepository statusChangeRepository,
                            SiteSettingsRepository settingsRepository,
                            GlassTypeRepository glassTypeRepository,
                            GlassColorRepository glassColorRepository,
                            GlassThicknessRepository glassThicknessRepository,
                            QuoteMapper mapper,
                            Validator validator,
                            Clock clock) {
        this.quoteRepository = quoteRepository;
        this.statusChangeRepository = statusChangeRepository;
        this.settingsRepository = settingsRepository;
        this.glassTypeRepository = glassTypeRepository;
        this.glassColorRepository = glassColorRepository;
        this.glassThicknessRepository = glassThicknessRepository;
        this.mapper = mapper;
        this.validator = validator;
        this.clock = clock;
    }

    /**
     * Valida aquí y no con {@code @Valid} en el controller porque el campo trampa va primero: a un bot se le
     * responde éxito aunque el resto de la solicitud sea inválido, para no darle pistas.
     */
    @Override
    @Transactional
    public QuoteCreatedResponse create(QuoteCreateRequest request, String sourceIp) {
        if (request.website() != null && !request.website().isBlank()) {
            return new QuoteCreatedResponse(UUID.randomUUID());
        }
        validate(request);
        if (!quotingEnabled()) {
            throw new ApiException(ErrorCode.QUOTING_DISABLED,
                    "En este momento no estamos recibiendo solicitudes de cotización por este medio.");
        }
        Quote quote = new Quote(UUID.randomUUID(), Instant.now(clock), sourceIp);
        mapper.apply(request, quote);
        if (request.hasConfiguration()) {
            applyConfiguration(request, quote);
        }
        return new QuoteCreatedResponse(quoteRepository.save(quote).getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuoteResponse> list(QuoteStatus status, int page, int size, boolean ascending) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER,
                    "La página debe ser 0 o más y el tamaño entre 1 y " + MAX_PAGE_SIZE + ".");
        }
        Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt").and(Sort.by(direction, "id")));
        Page<Quote> quotes = status == null
                ? quoteRepository.findAll(pageable)
                : quoteRepository.findByStatus(status, pageable);
        return PageResponse.from(quotes.map(mapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public QuoteDetailResponse get(UUID id) {
        Quote quote = find(id);
        return mapper.toDetailResponse(quote, statusChangeRepository.findByQuoteIdOrderByChangedAtAscIdAsc(id));
    }

    @Override
    @Transactional
    public QuoteResponse changeStatus(UUID id, QuoteStatus status) {
        Quote quote = find(id);
        if (quote.getStatus() != status) {
            Instant now = Instant.now(clock);
            QuoteStatus previous = quote.changeStatus(status, now);
            statusChangeRepository.save(new QuoteStatusChange(id, previous, status, now, null));
        }
        return mapper.toResponse(quote);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        find(id).markDeleted(Instant.now(clock));
    }

    private void validate(QuoteCreateRequest request) {
        validator.validate(request).stream()
                .min(Comparator.comparing((ConstraintViolation<QuoteCreateRequest> violation) ->
                        violation.getPropertyPath().toString()))
                .ifPresent(violation -> {
                    throw new ApiException(ErrorCode.VALIDATION_ERROR, violation.getMessage());
                });
    }

    private boolean quotingEnabled() {
        return settingsRepository.findById(SiteSettings.SINGLETON_ID).map(SiteSettings::isQuotingEnabled).orElse(false);
    }

    /**
     * Valida medidas y vidrio contra las opciones activas y recalcula área y texto de medidas en el servidor; el
     * texto que mandó el navegador se reemplaza.
     */
    private void applyConfiguration(QuoteCreateRequest request, Quote quote) {
        if (request.widthCm() == null || request.heightCm() == null || request.glassTypeCode() == null
                || request.glassColorCode() == null || request.thicknessMm() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR,
                    "Los datos del configurador están incompletos. Vuelve a aplicar las medidas.");
        }
        GlassType type = glassTypeRepository.findByCodeAndActiveTrue(request.glassTypeCode())
                .orElseThrow(() -> unavailable("El tipo de vidrio elegido ya no está disponible."));
        GlassColor color = glassColorRepository.findByCodeAndActiveTrue(request.glassColorCode())
                .orElseThrow(() -> unavailable("El color de vidrio elegido ya no está disponible."));
        GlassThickness thickness = glassThicknessRepository.findByMmAndActiveTrue(request.thicknessMm())
                .orElseThrow(() -> unavailable("El espesor elegido ya no está disponible."));

        int width = request.widthCm();
        int height = request.heightCm();
        int panes = request.panes() != null ? request.panes() : GlassConfiguratorRules.suggestedPanes(width);
        String glass = GlassConfiguratorRules.describeGlass(type.getName(), color.getName(), thickness.getMm());
        quote.setConfiguration(new Quote.Configuration(width, height, panes, GlassConfiguratorRules.areaM2(width, height),
                type.getCode(), color.getCode(), thickness.getMm()));
        quote.setDimensionsText(GlassConfiguratorRules.describeDimensions(width, height, panes, glass));
    }

    private static ApiException unavailable(String message) {
        return new ApiException(ErrorCode.VALIDATION_ERROR, message + " Actualiza la página y elige otra opción.");
    }

    /**
     * Las eliminadas ya quedan fuera de las consultas por {@code @SQLRestriction}; el filtro cubre una cotización
     * borrada en la misma transacción, que JPA devolvería desde memoria.
     */
    private Quote find(UUID id) {
        return quoteRepository.findById(id)
                .filter(quote -> quote.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("La cotización no existe."));
    }
}
