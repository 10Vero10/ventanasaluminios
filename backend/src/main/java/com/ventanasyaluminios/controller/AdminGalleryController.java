package com.ventanasyaluminios.controller;

import com.ventanasyaluminios.dto.request.GalleryImageRequest;
import com.ventanasyaluminios.dto.request.ReorderRequest;
import com.ventanasyaluminios.dto.response.GalleryImageResponse;
import com.ventanasyaluminios.service.GalleryService;
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

/** Galería de trabajos desde el panel. Se protege con rol ADMIN en el paso 4. */
@RestController
@RequestMapping("/api/v1/admin/gallery")
public class AdminGalleryController {

    private final GalleryService service;

    public AdminGalleryController(GalleryService service) {
        this.service = service;
    }

    @GetMapping
    public List<GalleryImageResponse> list() {
        return service.list(null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GalleryImageResponse create(@Valid @RequestBody GalleryImageRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public GalleryImageResponse update(@PathVariable Long id, @Valid @RequestBody GalleryImageRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/order")
    public List<GalleryImageResponse> reorder(@Valid @RequestBody ReorderRequest request) {
        return service.reorder(request.ids());
    }
}
