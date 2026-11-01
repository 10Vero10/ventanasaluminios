package com.ventanasyaluminios.dto.response;

import java.util.List;

/** Lo que el sitio público necesita al cargar: configuración y líneas destacadas en orden. */
public record PublicSiteResponse(
        String heroTitle,
        String heroSubtitle,
        String heroImageUrl,
        String heroBadge,
        String phone,
        String email,
        String whatsappNumber,
        int experienceYears,
        boolean quotingEnabled,
        String faviconUrl,
        String aboutParagraph1,
        String aboutParagraph2,
        String aboutHighlight,
        List<FeaturedLineResponse> featuredLines) {
}
