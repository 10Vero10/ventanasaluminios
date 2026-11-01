package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.GalleryImage;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Acceso a la galería de trabajos. */
public interface GalleryImageRepository extends JpaRepository<GalleryImage, Long> {

    List<GalleryImage> findAllByOrderByPositionAsc();

    List<GalleryImage> findByCategoryOrderByPositionAsc(ProductCategory category);

    @Query("select coalesce(max(g.position), 0) from GalleryImage g")
    int findMaxPosition();
}
