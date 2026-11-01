package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.FeaturedLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Acceso a las líneas destacadas de la portada. */
public interface FeaturedLineRepository extends JpaRepository<FeaturedLine, Long> {

    List<FeaturedLine> findAllByOrderByPositionAsc();

    @Query("select coalesce(max(f.position), 0) from FeaturedLine f")
    int findMaxPosition();
}
