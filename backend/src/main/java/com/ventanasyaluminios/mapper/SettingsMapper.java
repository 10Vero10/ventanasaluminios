package com.ventanasyaluminios.mapper;

import static com.ventanasyaluminios.mapper.TextNormalization.digitsOnly;
import static com.ventanasyaluminios.mapper.TextNormalization.trimOrDefault;
import static com.ventanasyaluminios.mapper.TextNormalization.trimToNull;

import com.ventanasyaluminios.dto.request.FeaturedLineRequest;
import com.ventanasyaluminios.dto.request.GalleryImageRequest;
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
 * Conversión entre entidades y DTO del módulo settings (configuración, líneas destacadas y galería). Toda
 * la limpieza de lo que llega del panel (espacios, vacíos, valores por defecto, dígitos) ocurre aquí.
 */
@Component
public class SettingsMapper {

    /** Título de una imagen de la galería cuando el administrador no escribe uno (briefing §3.3). */
    static final String DEFAULT_GALLERY_TITLE = "Imagen";

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
                settings.getAboutImageUrl(),
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
                settings.getAboutImageUrl(),
                lines.stream().map(this::toResponse).toList());
    }

    /** WhatsApp solo con dígitos; si viene vacío se toma del teléfono (briefing §3.1). */
    public String normalizedWhatsapp(SiteSettingsRequest request) {
        String whatsapp = digitsOnly(request.whatsappNumber());
        return whatsapp.isEmpty() ? digitsOnly(request.phone()) : whatsapp;
    }

    /** Copia los campos editables; el service valida antes el WhatsApp resultante. */
    public void apply(SiteSettingsRequest request, SiteSettings settings) {
        settings.setHeroTitle(request.heroTitle().trim());
        settings.setHeroSubtitle(request.heroSubtitle().trim());
        settings.setHeroImageUrl(trimToNull(request.heroImageUrl()));
        settings.setHeroBadge(trimToNull(request.heroBadge()));
        settings.setPhone(request.phone().trim());
        settings.setEmail(request.email().trim());
        settings.setWhatsappNumber(normalizedWhatsapp(request));
        settings.setExperienceYears(request.experienceYears());
        settings.setQuotingEnabled(request.quotingEnabled());
        settings.setFaviconUrl(trimToNull(request.faviconUrl()));
        settings.setAboutParagraph1(request.aboutParagraph1().trim());
        settings.setAboutParagraph2(request.aboutParagraph2().trim());
        settings.setAboutHighlight(request.aboutHighlight().trim());
        settings.setAboutImageUrl(trimToNull(request.aboutImageUrl()));
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
        line.setImageUrl(trimToNull(request.imageUrl()));
        line.setLinkText(request.linkText().trim());
        line.setTargetCategory(request.targetCategory());
    }

    public GalleryImageResponse toResponse(GalleryImage image) {
        return new GalleryImageResponse(image.getId(), image.getPosition(), image.getTitle(), image.getUrl(),
                image.getCategory());
    }

    public void apply(GalleryImageRequest request, GalleryImage image) {
        image.setTitle(trimOrDefault(request.title(), DEFAULT_GALLERY_TITLE));
        image.setUrl(request.url().trim());
        image.setCategory(request.category());
    }

    /** Configuración actual como solicitud, para el respaldo: el mismo formato que acepta el panel. */
    public SiteSettingsRequest toRequest(SiteSettingsResponse s) {
        return new SiteSettingsRequest(s.heroTitle(), s.heroSubtitle(), s.heroImageUrl(), s.heroBadge(), s.phone(),
                s.email(), s.whatsappNumber(), s.experienceYears(), s.quotingEnabled(), s.faviconUrl(),
                s.aboutParagraph1(), s.aboutParagraph2(), s.aboutHighlight(), s.aboutImageUrl());
    }

    public FeaturedLineRequest toRequest(FeaturedLineResponse l) {
        return new FeaturedLineRequest(l.number(), l.title(), l.categoryTag(), l.description(), l.imageUrl(),
                l.linkText(), l.targetCategory());
    }

    public GalleryImageRequest toRequest(GalleryImageResponse g) {
        return new GalleryImageRequest(g.title(), g.url(), g.category());
    }
}
