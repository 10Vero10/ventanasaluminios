package com.ventanasyaluminios.service.impl;

import com.ventanasyaluminios.dto.request.SiteSettingsRequest;
import com.ventanasyaluminios.dto.response.PublicSiteResponse;
import com.ventanasyaluminios.dto.response.SiteSettingsResponse;
import com.ventanasyaluminios.entity.SiteSettings;
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

    @Override
    @Transactional
    public SiteSettingsResponse updateSettings(SiteSettingsRequest request) {
        SiteSettings settings = load();
        mapper.apply(request, settings);
        settings.setWhatsappNumber(normalizeWhatsapp(request.whatsappNumber(), request.phone()));
        settings.setUpdatedAt(Instant.now(clock));
        return mapper.toResponse(settings);
    }

    /** WhatsApp queda solo con dígitos; si viene vacío se deriva del teléfono (briefing §3.1). */
    static String normalizeWhatsapp(String whatsappNumber, String phone) {
        String digits = whatsappNumber == null ? "" : whatsappNumber.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            digits = phone.replaceAll("\\D", "");
        }
        return digits;
    }

    /** La fila la crea la migración V2; si faltara, la base está mal inicializada. */
    private SiteSettings load() {
        return settingsRepository.findById(SiteSettings.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Falta la fila de site_settings (migración V2)."));
    }
}
