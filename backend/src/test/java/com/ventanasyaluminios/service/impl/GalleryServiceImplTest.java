package com.ventanasyaluminios.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GalleryServiceImplTest {

    @Mock
    private GalleryImageRepository repository;

    private GalleryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GalleryServiceImpl(repository, new SettingsMapper(),
                Clock.fixed(Instant.parse("2026-11-01T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void sinTituloSeGuardaComoImagen() {
        when(repository.findMaxPosition()).thenReturn(5);
        when(repository.save(any(GalleryImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new GalleryImageRequest("  ", "https://ejemplo.com/foto.jpg", null));

        assertThat(response.title()).isEqualTo("Imagen");
        assertThat(response.position()).isEqualTo(6);
    }

    @Test
    void conLineaSoloConsultaEsaLinea() {
        when(repository.findByCategoryOrderByPositionAsc(ProductCategory.PUERTAS)).thenReturn(List.of());

        service.list(ProductCategory.PUERTAS);

        verify(repository).findByCategoryOrderByPositionAsc(ProductCategory.PUERTAS);
        verify(repository, never()).findAllByOrderByPositionAsc();
    }
}
