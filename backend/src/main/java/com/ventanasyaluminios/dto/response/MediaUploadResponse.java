package com.ventanasyaluminios.dto.response;

/** Resultado de subir una imagen: la ruta pública donde quedó, por ejemplo {@code /media/3f2c….webp}. */
public record MediaUploadResponse(String url) {
}
