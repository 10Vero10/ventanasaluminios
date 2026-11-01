package com.ventanasyaluminios.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ventanasyaluminios.entity.GalleryImage;
import com.ventanasyaluminios.exception.ApiException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ReorderingTest {

    @Test
    void asignaPosicionesSegunLaListaYDevuelveElNuevoOrden() {
        GalleryImage a = image(10L, 1);
        GalleryImage b = image(20L, 2);
        GalleryImage c = image(30L, 3);

        List<GalleryImage> ordered = Reordering.apply(List.of(a, b, c), List.of(30L, 10L, 20L));

        assertThat(ordered).containsExactly(c, a, b);
        assertThat(c.getPosition()).isEqualTo(1);
        assertThat(a.getPosition()).isEqualTo(2);
        assertThat(b.getPosition()).isEqualTo(3);
    }

    @Test
    void rechazaListasIncompletasRepetidasOConIdsAjenos() {
        List<GalleryImage> current = List.of(image(1L, 1), image(2L, 2));

        for (List<Long> ids : List.of(List.of(1L), List.of(1L, 1L), List.of(1L, 3L), List.of(1L, 2L, 2L))) {
            assertThatThrownBy(() -> Reordering.apply(current, ids))
                    .isInstanceOf(ApiException.class)
                    .hasMessageStartingWith("El nuevo orden no coincide con los elementos actuales.");
        }
    }

    private static GalleryImage image(Long id, int position) {
        GalleryImage image = new GalleryImage();
        ReflectionTestUtils.setField(image, "id", id);
        image.setPosition(position);
        return image;
    }
}
