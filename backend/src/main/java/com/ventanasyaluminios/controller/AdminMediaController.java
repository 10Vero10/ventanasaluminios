package com.ventanasyaluminios.controller;

import com.ventanasyaluminios.dto.response.MediaUploadResponse;
import com.ventanasyaluminios.service.MediaStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Subida de imágenes desde el panel. Solo rol ADMIN. */
@RestController
@RequestMapping("/api/v1/admin/media")
public class AdminMediaController {

    private final MediaStorageService service;

    public AdminMediaController(MediaStorageService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaUploadResponse upload(@RequestPart("file") MultipartFile file) {
        return service.store(file);
    }
}
