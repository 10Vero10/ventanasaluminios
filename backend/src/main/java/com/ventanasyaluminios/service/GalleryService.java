package com.ventanasyaluminios.service;

import com.ventanasyaluminios.dto.request.GalleryImageRequest;
import com.ventanasyaluminios.dto.response.GalleryImageResponse;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import java.util.List;

/** Galería de trabajos. */
public interface GalleryService {

    /** Imágenes en orden; con {@code category} solo las de esa línea. */
    List<GalleryImageResponse> list(ProductCategory category);

    /** Agrega la imagen al final de la galería. */
    GalleryImageResponse create(GalleryImageRequest request);

    GalleryImageResponse update(Long id, GalleryImageRequest request);

    void delete(Long id);

    /** Aplica el orden recibido; debe incluir todos los ids existentes. */
    List<GalleryImageResponse> reorder(List<Long> ids);
}
