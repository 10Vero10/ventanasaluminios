package com.ventanasyaluminios.dto.response;

import java.time.Instant;

/** Configuración completa del sitio para el panel de administración. */
public record SiteSettingsResponse(
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
        Instant updatedAt) {
}
