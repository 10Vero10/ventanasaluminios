package com.ventanasyaluminios.dto.response;

/** Resumen de lo que quedó después de importar un respaldo. */
public record BackupImportResponse(int featuredLines, int galleryImages, int products, int glassTypes, int glassColors,
                                   int glassThicknesses) {
}
