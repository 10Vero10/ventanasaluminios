package com.ventanasyaluminios.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Página de resultados con lo justo para paginar en el panel; no expone el {@code Page} de Spring.
 *
 * @param page número de página, desde 0
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages());
    }
}
