package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.GlassColor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Acceso a los colores de vidrio del configurador. */
public interface GlassColorRepository extends JpaRepository<GlassColor, Long> {

    List<GlassColor> findAllByOrderByPositionAsc();

    List<GlassColor> findByActiveTrueOrderByPositionAsc();

    boolean existsByCode(String code);

    @Query("select coalesce(max(g.position), 0) from GlassColor g")
    int findMaxPosition();
}
