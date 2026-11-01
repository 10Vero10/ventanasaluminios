package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.GalleryImage;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import java.util.List;

/** Acceso a la galería de trabajos. */
public interface GalleryImageRepository extends PositionedRepository<GalleryImage> {

    List<GalleryImage> findByCategoryOrderByPositionAsc(ProductCategory category);
}
