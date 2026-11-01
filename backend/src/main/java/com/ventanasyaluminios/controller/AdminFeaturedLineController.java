package com.ventanasyaluminios.controller;

import com.ventanasyaluminios.dto.request.FeaturedLineRequest;
import com.ventanasyaluminios.dto.request.ReorderRequest;
import com.ventanasyaluminios.dto.response.FeaturedLineResponse;
import com.ventanasyaluminios.service.FeaturedLineService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Líneas destacadas de la portada desde el panel. Se protege con rol ADMIN en el paso 4. */
@RestController
@RequestMapping("/api/v1/admin/featured-lines")
public class AdminFeaturedLineController {

    private final FeaturedLineService service;

    public AdminFeaturedLineController(FeaturedLineService service) {
        this.service = service;
    }

    @GetMapping
    public List<FeaturedLineResponse> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeaturedLineResponse create(@Valid @RequestBody FeaturedLineRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public FeaturedLineResponse update(@PathVariable Long id, @Valid @RequestBody FeaturedLineRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/order")
    public List<FeaturedLineResponse> reorder(@Valid @RequestBody ReorderRequest request) {
        return service.reorder(request.ids());
    }
}
