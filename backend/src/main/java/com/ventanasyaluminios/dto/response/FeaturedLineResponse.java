package com.ventanasyaluminios.dto.response;

import com.ventanasyaluminios.entity.enums.ProductCategory;

/** Línea destacada de la portada. */
public record FeaturedLineResponse(
        Long id,
        int position,
        String number,
        String title,
        String categoryTag,
        String description,
        String imageUrl,
        String linkText,
        ProductCategory targetCategory) {
}
