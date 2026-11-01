package com.ventanasyaluminios.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ventanasyaluminios.dto.request.SiteSettingsRequest;
import com.ventanasyaluminios.dto.response.SiteSettingsResponse;
import com.ventanasyaluminios.entity.SiteSettings;
import com.ventanasyaluminios.mapper.SettingsMapper;
import com.ventanasyaluminios.repository.FeaturedLineRepository;
import com.ventanasyaluminios.repository.SiteSettingsRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SiteSettingsServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-11-01T12:00:00Z");

    @Mock
    private SiteSettingsRepository settingsRepository;

    @Mock
    private FeaturedLineRepository featuredLineRepository;

    private SiteSettingsServiceImpl service;
    private SiteSettings stored;

    @BeforeEach
    void setUp() {
        service = new SiteSettingsServiceImpl(settingsRepository, featuredLineRepository, new SettingsMapper(),
                Clock.fixed(NOW, ZoneOffset.UTC));
        stored = new SiteSettings();
        stored.setId(SiteSettings.SINGLETON_ID);
        when(settingsRepository.findById(SiteSettings.SINGLETON_ID)).thenReturn(Optional.of(stored));
    }

    @Test
    void whatsappSeGuardaSoloConDigitos() {
        SiteSettingsResponse response = service.updateSettings(request("+57 317 846 3260", "+57 317 846 3260"));

        assertThat(response.whatsappNumber()).isEqualTo("573178463260");
    }

    @Test
    void whatsappVacioSeDerivaDelTelefono() {
        SiteSettingsResponse response = service.updateSettings(request("+57 (300) 123-4567", "  "));

        assertThat(response.whatsappNumber()).isEqualTo("573001234567");
    }

    @Test
    void recortaEspaciosYGuardaOpcionalesVaciosComoNulos() {
        SiteSettingsResponse response = service.updateSettings(request("+57 317 846 3260", ""));

        assertThat(response.heroTitle()).isEqualTo("Ventanas y Aluminios SAS");
        assertThat(response.heroBadge()).isNull();
        assertThat(response.faviconUrl()).isNull();
        assertThat(response.updatedAt()).isEqualTo(NOW);
    }

    private SiteSettingsRequest request(String phone, String whatsapp) {
        return new SiteSettingsRequest("  Ventanas y Aluminios SAS  ", "Subtítulo", null, "  ", phone,
                "ventanaluminio@hotmail.com", whatsapp, 25, true, "", "Párrafo 1", "Párrafo 2", "Destacado");
    }
}
