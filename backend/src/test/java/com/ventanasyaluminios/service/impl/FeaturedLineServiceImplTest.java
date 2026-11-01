package com.ventanasyaluminios.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ventanasyaluminios.dto.request.FeaturedLineRequest;
import com.ventanasyaluminios.entity.FeaturedLine;
import com.ventanasyaluminios.entity.enums.ProductCategory;
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

    private static final FeaturedLineRequest REQUEST = new FeaturedLineRequest(" 05 ", "Fachadas", "VIDRIO",
            "Descripción", "  ", "Ver productos", ProductCategory.VENTANAS);

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

        var response = service.create(REQUEST);

        assertThat(response.position()).isEqualTo(5);
        assertThat(response.number()).isEqualTo("05");
        assertThat(response.imageUrl()).isNull();
    }

    @Test
    void editarConservaLaPosicion() {
        FeaturedLine line = line(7L, 3);
        when(repository.findById(7L)).thenReturn(Optional.of(line));

        var response = service.update(7L, REQUEST);

        assertThat(response.title()).isEqualTo("Fachadas");
        assertThat(response.position()).isEqualTo(3);
    }

    @Test
    void reordenarDevuelveLasLineasEnElNuevoOrden() {
        when(repository.findAll()).thenReturn(List.of(line(1L, 1), line(2L, 2)));

        var response = service.reorder(List.of(2L, 1L));

        assertThat(response).extracting(r -> r.id()).containsExactly(2L, 1L);
        assertThat(response).extracting(r -> r.position()).containsExactly(1, 2);
    }

    @Test
    void eliminarUnaLineaInexistenteDevuelve404() {
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
