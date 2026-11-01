package com.ventanasyaluminios.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Configuración general del sitio. Hay una sola fila, con id 1, creada por la migración V2. */
@Entity
@Table(name = "site_settings")
public class SiteSettings {

    /** Id fijo de la única fila. */
    public static final int SINGLETON_ID = 1;

    @Id
    private Integer id;

    @Column(name = "hero_title", nullable = false)
    private String heroTitle;

    @Column(name = "hero_subtitle", nullable = false)
    private String heroSubtitle;

    @Column(name = "hero_image_url")
    private String heroImageUrl;

    @Column(name = "hero_badge")
    private String heroBadge;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    @Column(name = "whatsapp_number", nullable = false)
    private String whatsappNumber;

    @Column(name = "experience_years", nullable = false)
    private int experienceYears;

    @Column(name = "quoting_enabled", nullable = false)
    private boolean quotingEnabled;

    @Column(name = "favicon_url")
    private String faviconUrl;

    @Column(name = "about_paragraph1", nullable = false)
    private String aboutParagraph1;

    @Column(name = "about_paragraph2", nullable = false)
    private String aboutParagraph2;

    @Column(name = "about_highlight", nullable = false)
    private String aboutHighlight;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Integer getId() {
        return id;
    }

    public String getHeroTitle() {
        return heroTitle;
    }

    public void setHeroTitle(String heroTitle) {
        this.heroTitle = heroTitle;
    }

    public String getHeroSubtitle() {
        return heroSubtitle;
    }

    public void setHeroSubtitle(String heroSubtitle) {
        this.heroSubtitle = heroSubtitle;
    }

    public String getHeroImageUrl() {
        return heroImageUrl;
    }

    public void setHeroImageUrl(String heroImageUrl) {
        this.heroImageUrl = heroImageUrl;
    }

    public String getHeroBadge() {
        return heroBadge;
    }

    public void setHeroBadge(String heroBadge) {
        this.heroBadge = heroBadge;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWhatsappNumber() {
        return whatsappNumber;
    }

    public void setWhatsappNumber(String whatsappNumber) {
        this.whatsappNumber = whatsappNumber;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public boolean isQuotingEnabled() {
        return quotingEnabled;
    }

    public void setQuotingEnabled(boolean quotingEnabled) {
        this.quotingEnabled = quotingEnabled;
    }

    public String getFaviconUrl() {
        return faviconUrl;
    }

    public void setFaviconUrl(String faviconUrl) {
        this.faviconUrl = faviconUrl;
    }

    public String getAboutParagraph1() {
        return aboutParagraph1;
    }

    public void setAboutParagraph1(String aboutParagraph1) {
        this.aboutParagraph1 = aboutParagraph1;
    }

    public String getAboutParagraph2() {
        return aboutParagraph2;
    }

    public void setAboutParagraph2(String aboutParagraph2) {
        this.aboutParagraph2 = aboutParagraph2;
    }

    public String getAboutHighlight() {
        return aboutHighlight;
    }

    public void setAboutHighlight(String aboutHighlight) {
        this.aboutHighlight = aboutHighlight;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
