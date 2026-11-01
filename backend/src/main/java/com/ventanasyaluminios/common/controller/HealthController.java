package com.ventanasyaluminios.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Usado por los healthchecks de Docker y nginx; no toca la base para no reiniciar el contenedor si Postgres se demora. */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    public record HealthResponse(String status) {
    }

    @GetMapping
    public HealthResponse health() {
        return new HealthResponse("ok");
    }
}
