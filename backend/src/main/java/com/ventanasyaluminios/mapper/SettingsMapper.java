package com.ventanasyaluminios.mapper;

import com.ventanasyaluminios.dto.request.FeaturedLineRequest;
import com.ventanasyaluminios.dto.request.SiteSettingsRequest;
import com.ventanasyaluminios.dto.response.FeaturedLineResponse;
import com.ventanasyaluminios.dto.response.GalleryImageResponse;
import com.ventanasyaluminios.dto.response.PublicSiteResponse;
import com.ventanasyaluminios.dto.response.SiteSettingsResponse;
import com.ventanasyaluminios.entity.FeaturedLine;
import com.ventanasyaluminios.entity.GalleryImage;
import com.ventanasyaluminios.entity.SiteSettings;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Conversión entre entidades y DTO del módulo settings (configuración, líneas destacadas y galería).
 * Recorta espacios y guarda los textos opcionales vacíos como {@code null}.
 */
@Component
public class SettingsMapper {

    public SiteSettingsResponse toResponse(SiteSettings settings) {
        return new SiteSettingsResponse(
                settings.getHeroTitle(),
                settings.getHeroSubtitle(),
                settings.getHeroImageUrl(),
                settings.getHeroBadge(),
                settings.getPhone(),
                settings.getEmail(),
                settings.getWhatsappNumber(),
                settings.getExperienceYears(),
                settings.isQuotingEnabled(),
                settings.getFaviconUrl(),
                settings.getAboutParagraph1(),
                settings.getAboutParagraph2(),
                settings.getAboutHighlight(),
                settings.getUpdatedAt());
    }

    public PublicSiteResponse toPublicResponse(SiteSettings settings, List<FeaturedLine> lines) {
        return new PublicSiteResponse(
                settings.getHeroTitle(),
                settings.getHeroSubtitle(),
                settings.getHeroImageUrl(),
                settings.getHeroBadge(),
                settings.getPhone(),
                settings.getEmail(),
                settings.getWhatsappNumber(),
                settings.getExperienceYears(),
                settings.isQuotingEnabled(),
                settings.getFaviconUrl(),
                settings.getAboutParagraph1(),
                settings.getAboutParagraph2(),
                settings.getAboutHighlight(),
                lines.stream().map(this::toResponse).toList());
    }

    /** Copia los campos editables; el WhatsApp normalizado lo decide el service. */
    public void apply(SiteSettingsRequest request, SiteSettings settings) {
        settings.setHeroTitle(request.heroTitle().trim());
        settings.setHeroSubtitle(request.heroSubtitle().trim());
        settings.setHeroImageUrl(blankToNull(request.heroImageUrl()));
        settings.setHeroBadge(blankToNull(request.heroBadge()));
        settings.setPhone(request.phone().trim());
        settings.setEmail(request.email().trim());
        settings.setExperienceYears(request.experienceYears().shortValue());
        settings.setQuotingEnabled(request.quotingEnabled());
        settings.setFaviconUrl(blankToNull(request.faviconUrl()));
        settings.setAboutParagraph1(request.aboutParagraph1().trim());
        settings.setAboutParagraph2(request.aboutParagraph2().trim());
        settings.setAboutHighlight(request.aboutHighlight().trim());
    }

    public FeaturedLineResponse toResponse(FeaturedLine line) {
        return new FeaturedLineResponse(
                line.getId(),
                line.getPosition(),
                line.getNumber(),
                line.getTitle(),
                line.getCategoryTag(),
                line.getDescription(),
                line.getImageUrl(),
                line.getLinkText(),
                line.getTargetCategory());
    }

    public void apply(FeaturedLineRequest request, FeaturedLine line) {
        line.setNumber(request.number().trim());
        line.setTitle(request.title().trim());
        line.setCategoryTag(request.categoryTag().trim());
        line.setDescription(request.description().trim());
        line.setImageUrl(blankToNull(request.imageUrl()));
        line.setLinkText(request.linkText().trim());
        line.setTargetCategory(request.targetCategory());
    }

    public GalleryImageResponse toResponse(GalleryImage image) {
        return new GalleryImageResponse(image.getId(), image.getPosition(), image.getTitle(), image.getUrl(),
                image.getCategory());
    }

    static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
