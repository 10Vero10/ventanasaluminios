package com.ventanasyaluminios.service;

import com.ventanasyaluminios.dto.request.SiteSettingsRequest;
import com.ventanasyaluminios.dto.response.PublicSiteResponse;
import com.ventanasyaluminios.dto.response.SiteSettingsResponse;

/** Configuración general del sitio. */
public interface SiteSettingsService {

    /** Configuración pública y líneas destacadas, para el sitio. */
    PublicSiteResponse getPublicSite();

    /** Configuración completa, para el panel. */
    SiteSettingsResponse getSettings();

    /** Guarda la configuración; el cambio se ve al instante en el sitio. */
    SiteSettingsResponse updateSettings(SiteSettingsRequest request);
}
