package com.ventanasyaluminios.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ventanasyaluminios.dto.request.GalleryImageRequest;
import com.ventanasyaluminios.entity.GalleryImage;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import com.ventanasyaluminios.mapper.SettingsMapper;
import com.ventanasyaluminios.repository.GalleryImageRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GalleryServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-11-01T12:00:00Z");

    @Mock
    private GalleryImageRepository repository;

    private GalleryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GalleryServiceImpl(repository, new SettingsMapper(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void sinTituloSeGuardaComoImagenAlFinalYConFecha() {
        when(repository.findMaxPosition()).thenReturn(5);
        when(repository.save(any(GalleryImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new GalleryImageRequest("  ", " https://ejemplo.com/foto.jpg ", null));

        assertThat(response.title()).isEqualTo("Imagen");
        assertThat(response.url()).isEqualTo("https://ejemplo.com/foto.jpg");
        assertThat(response.position()).isEqualTo(6);
    }

    @Test
    void editarCambiaTituloYLinea() {
        GalleryImage image = image(3L, 2);
        when(repository.findById(3L)).thenReturn(Optional.of(image));

        var response = service.update(3L,
                new GalleryImageRequest("Portón negro", "https://ejemplo.com/p.jpg", ProductCategory.PORTONES));

        assertThat(response.title()).isEqualTo("Portón negro");
        assertThat(response.category()).isEqualTo(ProductCategory.PORTONES);
        assertThat(response.position()).isEqualTo(2);
    }

    @Test
    void listarPorLineaSoloDevuelveEsaLinea() {
        GalleryImage puerta = image(4L, 1);
        puerta.setCategory(ProductCategory.PUERTAS);
        when(repository.findByCategoryOrderByPositionAsc(ProductCategory.PUERTAS)).thenReturn(List.of(puerta));

        assertThat(service.listByCategory(ProductCategory.PUERTAS)).extracting(r -> r.id()).containsExactly(4L);
    }

    @Test
    void reordenarDevuelveLaGaleriaEnElNuevoOrden() {
        when(repository.findAll()).thenReturn(List.of(image(1L, 1), image(2L, 2)));

        assertThat(service.reorder(List.of(2L, 1L))).extracting(r -> r.id()).containsExactly(2L, 1L);
    }

    private static GalleryImage image(Long id, int position) {
        GalleryImage image = new GalleryImage();
        ReflectionTestUtils.setField(image, "id", id);
        image.setPosition(position);
        image.setTitle("Imagen");
        image.setUrl("https://ejemplo.com/" + id + ".jpg");
        return image;
    }
}
