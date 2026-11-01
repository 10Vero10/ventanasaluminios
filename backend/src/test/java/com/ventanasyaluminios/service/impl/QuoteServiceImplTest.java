package com.ventanasyaluminios.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ventanasyaluminios.dto.request.QuoteCreateRequest;
import com.ventanasyaluminios.entity.GlassColor;
import com.ventanasyaluminios.entity.GlassThickness;
import com.ventanasyaluminios.entity.GlassType;
import com.ventanasyaluminios.entity.Quote;
import com.ventanasyaluminios.entity.QuoteStatusChange;
import com.ventanasyaluminios.entity.SiteSettings;
import com.ventanasyaluminios.entity.enums.QuoteStatus;
import com.ventanasyaluminios.exception.ApiException;
import com.ventanasyaluminios.exception.ErrorCode;
import com.ventanasyaluminios.mapper.QuoteMapper;
import com.ventanasyaluminios.repository.GlassColorRepository;
import com.ventanasyaluminios.repository.GlassThicknessRepository;
import com.ventanasyaluminios.repository.GlassTypeRepository;
import com.ventanasyaluminios.repository.QuoteRepository;
import com.ventanasyaluminios.repository.QuoteStatusChangeRepository;
import com.ventanasyaluminios.repository.SiteSettingsRepository;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuoteServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-11-01T12:00:00Z");

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private QuoteStatusChangeRepository statusChangeRepository;

    @Mock
    private SiteSettingsRepository settingsRepository;

    @Mock
    private GlassTypeRepository glassTypeRepository;

    @Mock
    private GlassColorRepository glassColorRepository;

    @Mock
    private GlassThicknessRepository glassThicknessRepository;

    private QuoteServiceImpl service;
    private final SiteSettings settings = new SiteSettings();

    @BeforeEach
    void setUp() {
        service = new QuoteServiceImpl(quoteRepository, statusChangeRepository, settingsRepository, glassTypeRepository,
                glassColorRepository, glassThicknessRepository, new QuoteMapper(),
                Validation.buildDefaultValidatorFactory().getValidator(), Clock.fixed(NOW, ZoneOffset.UTC));
        settings.setQuotingEnabled(true);
        lenient().when(settingsRepository.findById(SiteSettings.SINGLETON_ID)).thenReturn(Optional.of(settings));
        lenient().when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void elCampoTrampaRespondeExitoSinGuardarAunqueTodoLoDemasSeaInvalido() {
        var response = service.create(new QuoteCreateRequest("", "", null, null, null, 9999, null, null, null, null, null,
                "https://spam.example"), "1.1.1.1");

        assertThat(response.id()).isNotNull();
        verify(quoteRepository, never()).save(any());
    }

    @Test
    void sinNombreOTelefonoSeRechazaConElMensajeDelBriefing() {
        assertThatThrownBy(() -> service.create(simple("  ", "+57 300"), "1.1.1.1"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Nombre y teléfono son obligatorios.");
    }

    @Test
    void conLaCotizacionApagadaSeRechazaEnElServidor() {
        settings.setQuotingEnabled(false);

        assertThatThrownBy(() -> service.create(simple("Carlos", "+57 300"), "1.1.1.1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo(ErrorCode.QUOTING_DISABLED);
        verify(quoteRepository, never()).save(any());
    }

    @Test
    void guardaRecortandoEspaciosConEstadoNuevaYLaIp() {
        service.create(new QuoteCreateRequest(" Carlos ", " +57 300 ", " Ventanas arquitectónicas ", " 120 x 150 ",
                "  ", null, null, null, null, null, null, ""), "10.0.0.5");

        Quote saved = savedQuote();
        assertThat(saved.getName()).isEqualTo("Carlos");
        assertThat(saved.getProductType()).isEqualTo("Ventanas arquitectónicas");
        assertThat(saved.getDimensionsText()).isEqualTo("120 x 150");
        assertThat(saved.getMessage()).isNull();
        assertThat(saved.getStatus()).isEqualTo(QuoteStatus.NUEVA);
        assertThat(saved.getSourceIp()).isEqualTo("10.0.0.5");
        assertThat(saved.getCreatedAt()).isEqualTo(NOW);
        assertThat(saved.getWidthCm()).isNull();
    }

    @Test
    void conElConfiguradorElServidorRecalculaAreaYMedidasYProponeLasHojas() {
        stubGlass();

        service.create(new QuoteCreateRequest("Carlos", "+57 300", "Ventanas", "texto del navegador", null, 450, 200, null,
                "LAMINADO_TEMPLADO", "BRONCE", 8, null), "1.1.1.1");

        Quote saved = savedQuote();
        assertThat(saved.getAreaM2()).isEqualTo(new BigDecimal("9.00"));
        assertThat(saved.getPanes()).isEqualTo(3);
        assertThat(saved.getDimensionsText())
                .isEqualTo("200 cm alto x 450 cm ancho (9.00 m²) · Vidrio laminado templado bronce 8 mm · 3 hojas");
    }

    @Test
    void lasHojasQueEligeElClienteSeRespetan() {
        stubGlass();

        service.create(new QuoteCreateRequest("Carlos", "+57 300", null, null, null, 450, 200, 1, "LAMINADO_TEMPLADO",
                "BRONCE", 8, null), "1.1.1.1");

        assertThat(savedQuote().getDimensionsText()).doesNotContain("hojas");
    }

    @Test
    void datosDelConfiguradorIncompletosSeRechazan() {
        assertThatThrownBy(() -> service.create(new QuoteCreateRequest("Carlos", "+57 300", null, null, null, 150, null,
                null, "CRUDO", null, null, null), "1.1.1.1"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Los datos del configurador están incompletos. Vuelve a aplicar las medidas.");
    }

    @Test
    void unaOpcionDeVidrioInactivaOInexistenteSeRechaza() {
        when(glassTypeRepository.findByCodeAndActiveTrue("VIEJO")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new QuoteCreateRequest("Carlos", "+57 300", null, null, null, 150, 120,
                null, "VIEJO", "BRONCE", 8, null), "1.1.1.1"))
                .isInstanceOf(ApiException.class)
                .hasMessageStartingWith("El tipo de vidrio elegido ya no está disponible.");
    }

    @Test
    void cambiarDeEstadoQuedaEnElHistorialYRepetirElMismoNo() {
        Quote quote = new Quote(UUID.randomUUID(), NOW.minusSeconds(60), "1.1.1.1");
        when(quoteRepository.findById(quote.getId())).thenReturn(Optional.of(quote));

        service.changeStatus(quote.getId(), QuoteStatus.CONTACTADA, 7L);
        service.changeStatus(quote.getId(), QuoteStatus.CONTACTADA, 7L);

        ArgumentCaptor<QuoteStatusChange> change = ArgumentCaptor.forClass(QuoteStatusChange.class);
        verify(statusChangeRepository).save(change.capture());
        assertThat(change.getValue().getFromStatus()).isEqualTo(QuoteStatus.NUEVA);
        assertThat(change.getValue().getToStatus()).isEqualTo(QuoteStatus.CONTACTADA);
        assertThat(change.getValue().getChangedBy()).isEqualTo(7L);
        assertThat(quote.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void eliminarMarcaLaFechaDeBorrado() {
        Quote quote = new Quote(UUID.randomUUID(), NOW, "1.1.1.1");
        when(quoteRepository.findById(quote.getId())).thenReturn(Optional.of(quote));

        service.delete(quote.getId());

        assertThat(quote.getDeletedAt()).isEqualTo(NOW);
    }

    @Test
    void paginaOTamanoInvalidosSeRechazan() {
        assertThatThrownBy(() -> service.list(null, -1, 20, true)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> service.list(null, 0, 101, true)).isInstanceOf(ApiException.class);
    }

    private void stubGlass() {
        GlassType type = new GlassType("LAMINADO_TEMPLADO");
        type.setName("Laminado templado");
        GlassColor color = new GlassColor("BRONCE");
        color.setName("Bronce");
        when(glassTypeRepository.findByCodeAndActiveTrue("LAMINADO_TEMPLADO")).thenReturn(Optional.of(type));
        when(glassColorRepository.findByCodeAndActiveTrue("BRONCE")).thenReturn(Optional.of(color));
        when(glassThicknessRepository.findByMmAndActiveTrue(8)).thenReturn(Optional.of(new GlassThickness(8)));
    }

    private Quote savedQuote() {
        ArgumentCaptor<Quote> captor = ArgumentCaptor.forClass(Quote.class);
        verify(quoteRepository).save(captor.capture());
        return captor.getValue();
    }

    private static QuoteCreateRequest simple(String name, String phone) {
        return new QuoteCreateRequest(name, phone, null, null, null, null, null, null, null, null, null, null);
    }
}
