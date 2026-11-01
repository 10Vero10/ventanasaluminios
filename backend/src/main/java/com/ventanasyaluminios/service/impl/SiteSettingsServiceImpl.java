package com.ventanasyaluminios.service.impl;

import com.ventanasyaluminios.dto.request.SiteSettingsRequest;
import com.ventanasyaluminios.dto.response.PublicSiteResponse;
import com.ventanasyaluminios.dto.response.SiteSettingsResponse;
import com.ventanasyaluminios.entity.SiteSettings;
import com.ventanasyaluminios.exception.ApiException;
import com.ventanasyaluminios.exception.ErrorCode;
import com.ventanasyaluminios.mapper.SettingsMapper;
import com.ventanasyaluminios.repository.FeaturedLineRepository;
import com.ventanasyaluminios.repository.SiteSettingsRepository;
import com.ventanasyaluminios.service.SiteSettingsService;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de {@link SiteSettingsService}. */
@Service
public class SiteSettingsServiceImpl implements SiteSettingsService {

    /** Un número con indicativo de país tiene entre 7 y 20 dígitos (E.164 admite hasta 15). */
    static final int WHATSAPP_MIN_DIGITS = 7;
    static final int WHATSAPP_MAX_DIGITS = 20;

    private final SiteSettingsRepository settingsRepository;
    private final FeaturedLineRepository featuredLineRepository;
    private final SettingsMapper mapper;
    private final Clock clock;

    public SiteSettingsServiceImpl(SiteSettingsRepository settingsRepository,
                                   FeaturedLineRepository featuredLineRepository,
                                   SettingsMapper mapper,
                                   Clock clock) {
        this.settingsRepository = settingsRepository;
        this.featuredLineRepository = featuredLineRepository;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PublicSiteResponse getPublicSite() {
        return mapper.toPublicResponse(load(), featuredLineRepository.findAllByOrderByPositionAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public SiteSettingsResponse getSettings() {
        return mapper.toResponse(load());
    }

    /** Valida antes de modificar la entidad: si algo no es válido, no queda ningún cambio a medias. */
    @Override
    @Transactional
    public SiteSettingsResponse updateSettings(SiteSettingsRequest request) {
        int digits = mapper.normalizedWhatsapp(request).length();
        if (digits < WHATSAPP_MIN_DIGITS || digits > WHATSAPP_MAX_DIGITS) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "El número de WhatsApp debe tener entre "
                    + WHATSAPP_MIN_DIGITS + " y " + WHATSAPP_MAX_DIGITS
                    + " dígitos con el indicativo de país (ej. 573178463260). Si lo dejas vacío se toma del teléfono.");
        }
        SiteSettings settings = load();
        mapper.apply(request, settings);
        settings.setUpdatedAt(Instant.now(clock));
        return mapper.toResponse(settings);
    }

    /** La fila la crea la migración V2; si faltara, la base está mal inicializada. */
    private SiteSettings load() {
        return settingsRepository.findById(SiteSettings.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Falta la fila de site_settings (migración V2)."));
    }
}
