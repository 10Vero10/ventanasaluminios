package com.ventanasyaluminios.controller;

import com.ventanasyaluminios.dto.response.GalleryImageResponse;
import com.ventanasyaluminios.dto.response.PublicSiteResponse;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import com.ventanasyaluminios.service.GalleryService;
import com.ventanasyaluminios.service.SiteSettingsService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints públicos del contenido del sitio: configuración, líneas destacadas y galería de trabajos. */
@RestController
@RequestMapping("/api/v1")
public class SiteController {

    private final SiteSettingsService siteSettingsService;
    private final GalleryService galleryService;

    public SiteController(SiteSettingsService siteSettingsService, GalleryService galleryService) {
        this.siteSettingsService = siteSettingsService;
        this.galleryService = galleryService;
    }

    @GetMapping("/site")
    public PublicSiteResponse site() {
        return siteSettingsService.getPublicSite();
    }

    @GetMapping("/gallery")
    public List<GalleryImageResponse> gallery(@RequestParam(required = false) ProductCategory category) {
        return galleryService.list(category);
    }
}
