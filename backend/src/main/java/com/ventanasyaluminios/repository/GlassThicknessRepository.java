package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.GlassThickness;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Acceso a los espesores de vidrio del configurador. */
public interface GlassThicknessRepository extends JpaRepository<GlassThickness, Long> {

    List<GlassThickness> findAllByOrderByPositionAsc();

    List<GlassThickness> findByActiveTrueOrderByPositionAsc();

    @Query("select coalesce(max(g.position), 0) from GlassThickness g")
    int findMaxPosition();
}
