package com.ventanasyaluminios.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.ventanasyaluminios.dto.request.GlassColorRequest;
import com.ventanasyaluminios.dto.request.GlassThicknessRequest;
import com.ventanasyaluminios.dto.request.GlassTypeRequest;
import com.ventanasyaluminios.entity.GlassThickness;
import com.ventanasyaluminios.entity.GlassType;
import com.ventanasyaluminios.exception.ApiException;
import com.ventanasyaluminios.exception.ErrorCode;
import com.ventanasyaluminios.mapper.CatalogMapper;
import com.ventanasyaluminios.repository.GlassColorRepository;
import com.ventanasyaluminios.repository.GlassThicknessRepository;
import com.ventanasyaluminios.repository.GlassTypeRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GlassOptionServiceImplTest {

    @Mock
    private GlassTypeRepository typeRepository;

    @Mock
    private GlassColorRepository colorRepository;

    @Mock
    private GlassThicknessRepository thicknessRepository;

    private GlassOptionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GlassOptionServiceImpl(typeRepository, colorRepository, thicknessRepository, new CatalogMapper());
    }

    @Test
    void unCodigoRepetidoSeRechazaCon409() {
        when(colorRepository.existsByCode("BRONCE")).thenReturn(true);

        assertThatThrownBy(() -> service.createColor(
                new GlassColorRequest("BRONCE", "Bronce", "#966969", false, null, true, false)))
                .isInstanceOf(ApiException.class)
                .hasMessage("Ya existe un color de vidrio con el código BRONCE.")
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo(ErrorCode.DUPLICATE_CODE);
    }

    @Test
    void elCodigoNoSePuedeCambiarAlEditar() {
        when(typeRepository.findById(1L)).thenReturn(Optional.of(type(1L, "CRUDO", true, true)));

        assertThatThrownBy(() -> service.updateType(1L, new GlassTypeRequest("CRUDO_NUEVO", "Crudo", true, true)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo(ErrorCode.IMMUTABLE_FIELD);
    }

    @Test
    void laOpcionPorDefectoDebeEstarActiva() {
        when(thicknessRepository.existsByMm(19)).thenReturn(false);

        assertThatThrownBy(() -> service.createThickness(new GlassThicknessRequest(19, false, true)))
                .isInstanceOf(ApiException.class)
                .hasMessage("La opción por defecto debe estar activa.");
    }

    @Test
    void marcarOtraPorDefectoDesmarcaLaAnteriorYLaEscribeAntes() {
        GlassType crudo = type(1L, "CRUDO", true, true);
        GlassType templado = type(3L, "TEMPLADO", true, false);
        when(typeRepository.findById(3L)).thenReturn(Optional.of(templado));
        when(typeRepository.findAll()).thenReturn(List.of(crudo, templado));

        var response = service.updateType(3L, new GlassTypeRequest("TEMPLADO", "Templado", true, true));

        assertThat(response.defaultOption()).isTrue();
        assertThat(crudo.isDefaultOption()).isFalse();
        InOrder order = inOrder(typeRepository);
        order.verify(typeRepository).findAll();
        order.verify(typeRepository).flush();
    }

    @Test
    void unEspesorNuevoQuedaAlFinal() {
        when(thicknessRepository.existsByMm(19)).thenReturn(false);
        when(thicknessRepository.findMaxPosition()).thenReturn(7);
        when(thicknessRepository.save(any(GlassThickness.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createThickness(new GlassThicknessRequest(19, true, false));

        assertThat(response.mm()).isEqualTo(19);
        assertThat(response.position()).isEqualTo(8);
        assertThat(response.defaultOption()).isFalse();
    }

    private static GlassType type(Long id, String code, boolean active, boolean defaultOption) {
        GlassType type = new GlassType();
        ReflectionTestUtils.setField(type, "id", id);
        type.setCode(code);
        type.setName(code);
        type.setActive(active);
        type.setDefaultOption(defaultOption);
        return type;
    }
}
