package com.ventanasyaluminios.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ventanasyaluminios.dto.request.SiteSettingsRequest;
import com.ventanasyaluminios.dto.response.SiteSettingsResponse;
import com.ventanasyaluminios.entity.SiteSettings;
import com.ventanasyaluminios.exception.ApiException;
import com.ventanasyaluminios.exception.ErrorCode;
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

    @BeforeEach
    void setUp() {
        service = new SiteSettingsServiceImpl(settingsRepository, featuredLineRepository, new SettingsMapper(),
                Clock.fixed(NOW, ZoneOffset.UTC));
        // Las pruebas de rechazo no llegan a cargar la configuración: se valida antes de tocarla.
        lenient().when(settingsRepository.findById(SiteSettings.SINGLETON_ID)).thenReturn(Optional.of(new SiteSettings()));
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
    void whatsappDerivadoDeUnTelefonoLargoSeRechazaConMensajeClaro() {
        assertThatThrownBy(() -> service.updateSettings(request("+57 300 111 2233 444 555 666 777 88", "")))
                .isInstanceOf(ApiException.class)
                .hasMessageStartingWith("El número de WhatsApp debe tener entre 7 y 20 dígitos")
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void whatsappDemasiadoCortoSeRechazaSinTocarLaConfiguracion() {
        assertThatThrownBy(() -> service.updateSettings(request("+57 317 846 3260", "123")))
                .isInstanceOf(ApiException.class);
        verify(settingsRepository, never()).findById(SiteSettings.SINGLETON_ID);
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
