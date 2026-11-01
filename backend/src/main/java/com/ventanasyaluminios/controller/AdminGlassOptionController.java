package com.ventanasyaluminios.controller;

import com.ventanasyaluminios.dto.request.GlassColorRequest;
import com.ventanasyaluminios.dto.request.GlassThicknessRequest;
import com.ventanasyaluminios.dto.request.GlassTypeRequest;
import com.ventanasyaluminios.dto.request.ReorderRequest;
import com.ventanasyaluminios.dto.response.GlassColorResponse;
import com.ventanasyaluminios.dto.response.GlassOptionsResponse;
import com.ventanasyaluminios.dto.response.GlassThicknessResponse;
import com.ventanasyaluminios.dto.response.GlassTypeResponse;
import com.ventanasyaluminios.service.GlassOptionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Opciones del configurador de vidrio desde el panel. No hay DELETE: una opción se desactiva con
 * {@code active: false}. Se protege con rol ADMIN en el paso 4.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminGlassOptionController {

    private final GlassOptionService service;

    public AdminGlassOptionController(GlassOptionService service) {
        this.service = service;
    }

    @GetMapping("/glass-options")
    public GlassOptionsResponse all() {
        return service.getAllOptions();
    }

    @PostMapping("/glass-types")
    @ResponseStatus(HttpStatus.CREATED)
    public GlassTypeResponse createType(@Valid @RequestBody GlassTypeRequest request) {
        return service.createType(request);
    }

    @PutMapping("/glass-types/{id}")
    public GlassTypeResponse updateType(@PathVariable Long id, @Valid @RequestBody GlassTypeRequest request) {
        return service.updateType(id, request);
    }

    @PutMapping("/glass-types/order")
    public List<GlassTypeResponse> reorderTypes(@Valid @RequestBody ReorderRequest request) {
        return service.reorderTypes(request.ids());
    }

    @PostMapping("/glass-colors")
    @ResponseStatus(HttpStatus.CREATED)
    public GlassColorResponse createColor(@Valid @RequestBody GlassColorRequest request) {
        return service.createColor(request);
    }

    @PutMapping("/glass-colors/{id}")
    public GlassColorResponse updateColor(@PathVariable Long id, @Valid @RequestBody GlassColorRequest request) {
        return service.updateColor(id, request);
    }

    @PutMapping("/glass-colors/order")
    public List<GlassColorResponse> reorderColors(@Valid @RequestBody ReorderRequest request) {
        return service.reorderColors(request.ids());
    }

    @PostMapping("/glass-thicknesses")
    @ResponseStatus(HttpStatus.CREATED)
    public GlassThicknessResponse createThickness(@Valid @RequestBody GlassThicknessRequest request) {
        return service.createThickness(request);
    }

    @PutMapping("/glass-thicknesses/{id}")
    public GlassThicknessResponse updateThickness(@PathVariable Long id,
                                                  @Valid @RequestBody GlassThicknessRequest request) {
        return service.updateThickness(id, request);
    }

    @PutMapping("/glass-thicknesses/order")
    public List<GlassThicknessResponse> reorderThicknesses(@Valid @RequestBody ReorderRequest request) {
        return service.reorderThicknesses(request.ids());
    }
}
