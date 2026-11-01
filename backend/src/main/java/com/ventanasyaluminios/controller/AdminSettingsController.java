package com.ventanasyaluminios.controller;

import com.ventanasyaluminios.dto.request.SiteSettingsRequest;
import com.ventanasyaluminios.dto.response.SiteSettingsResponse;
import com.ventanasyaluminios.service.SiteSettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Configuración general del sitio desde el panel. Se protege con rol ADMIN en el paso 4. */
@RestController
@RequestMapping("/api/v1/admin/settings")
public class AdminSettingsController {

    private final SiteSettingsService service;

    public AdminSettingsController(SiteSettingsService service) {
        this.service = service;
    }

    @GetMapping
    public SiteSettingsResponse get() {
        return service.getSettings();
    }

    @PutMapping
    public SiteSettingsResponse update(@Valid @RequestBody SiteSettingsRequest request) {
        return service.updateSettings(request);
    }
}
