package com.ventanasyaluminios.controller;

import com.ventanasyaluminios.dto.response.DashboardResponse;
import com.ventanasyaluminios.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Indicadores del encabezado del panel. Se protege con rol ADMIN en el paso 4. */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {

    private final DashboardService service;

    public AdminDashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardResponse get() {
        return service.get();
    }
}
