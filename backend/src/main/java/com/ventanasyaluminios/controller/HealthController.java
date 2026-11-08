package com.ventanasyaluminios.controller;

import com.ventanasyaluminios.dto.response.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lo llaman el health check de Render y el despertador externo (cada 10 minutos, para que el servicio gratis no se
 * duerma). No debe tocar la base: así Neon se suspende cuando no hay visitas reales (ADR-022).
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public HealthResponse health() {
        return new HealthResponse("ok");
    }
}
