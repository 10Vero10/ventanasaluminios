package com.ventanasyaluminios.service;

import com.ventanasyaluminios.dto.response.MediaUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/** Guarda imágenes subidas desde el panel en la carpeta del servidor (volumen de Docker). */
public interface MediaStorageService {

    /**
     * Valida y guarda la imagen con un nombre aleatorio.
     *
     * @return ruta pública de la imagen, servida en {@code /media/**}
     */
    MediaUploadResponse store(MultipartFile file);
}
