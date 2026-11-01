package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.GlassType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Acceso a los tipos de vidrio del configurador. */
public interface GlassTypeRepository extends JpaRepository<GlassType, Long> {

    List<GlassType> findAllByOrderByPositionAsc();

    List<GlassType> findByActiveTrueOrderByPositionAsc();

    boolean existsByCode(String code);

    @Query("select coalesce(max(g.position), 0) from GlassType g")
    int findMaxPosition();
}
