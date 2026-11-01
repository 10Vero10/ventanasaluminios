package com.ventanasyaluminios.dto.response;

import com.ventanasyaluminios.entity.enums.ProductCategory;

/** Imagen de la galería de trabajos; {@code category} es nula si no pertenece a una línea. */
public record GalleryImageResponse(Long id, int position, String title, String url, ProductCategory category) {
}
