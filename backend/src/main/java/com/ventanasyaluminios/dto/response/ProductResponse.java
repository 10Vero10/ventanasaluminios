package com.ventanasyaluminios.dto.response;

import com.ventanasyaluminios.entity.enums.ProductCategory;
import java.util.List;

/** Producto del catálogo con su ficha técnica y características en orden. */
public record ProductResponse(
        Long id,
        int position,
        ProductCategory category,
        String categoryNumber,
        String title,
        String subtitle,
        String description,
        String imageUrl,
        String badge,
        String ctaText,
        boolean active,
        List<ProductSpecResponse> specs,
        List<String> features) {
}
