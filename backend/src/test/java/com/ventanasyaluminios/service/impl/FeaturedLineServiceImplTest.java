package com.ventanasyaluminios.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ventanasyaluminios.dto.request.FeaturedLineRequest;
import com.ventanasyaluminios.entity.FeaturedLine;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import com.ventanasyaluminios.exception.InvalidRequestException;
import com.ventanasyaluminios.exception.ResourceNotFoundException;
import com.ventanasyaluminios.mapper.SettingsMapper;
import com.ventanasyaluminios.repository.FeaturedLineRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FeaturedLineServiceImplTest {

    @Mock
    private FeaturedLineRepository repository;

    private FeaturedLineServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FeaturedLineServiceImpl(repository, new SettingsMapper());
    }

    @Test
    void laLineaNuevaQuedaAlFinal() {
        when(repository.findMaxPosition()).thenReturn(4);
        when(repository.save(any(FeaturedLine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new FeaturedLineRequest("05", "Fachadas", "VIDRIO", "Descripción", null,
                "Ver productos", ProductCategory.VENTANAS));

        assertThat(response.position()).isEqualTo(5);
    }

    @Test
    void reordenarAsignaPosicionesSegunLaLista() {
        FeaturedLine a = line(1L, 1);
        FeaturedLine b = line(2L, 2);
        when(repository.findAll()).thenReturn(List.of(a, b));
        when(repository.findAllByOrderByPositionAsc()).thenReturn(List.of(b, a));

        service.reorder(List.of(2L, 1L));

        assertThat(a.getPosition()).isEqualTo(2);
        assertThat(b.getPosition()).isEqualTo(1);
    }

    @Test
    void reordenarRechazaListasIncompletasORepetidas() {
        when(repository.findAll()).thenReturn(List.of(line(1L, 1), line(2L, 2)));

        assertThatThrownBy(() -> service.reorder(List.of(1L))).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.reorder(List.of(1L, 1L))).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.reorder(List.of(1L, 3L))).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void editarUnaLineaInexistenteDevuelve404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La línea destacada no existe.");
    }

    private static FeaturedLine line(Long id, int position) {
        FeaturedLine line = new FeaturedLine();
        ReflectionTestUtils.setField(line, "id", id);
        line.setPosition(position);
        return line;
    }
}
