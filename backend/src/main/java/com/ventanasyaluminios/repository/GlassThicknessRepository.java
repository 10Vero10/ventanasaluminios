package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.GlassThickness;

/** Acceso a los espesores de vidrio del configurador. */
public interface GlassThicknessRepository extends GlassOptionRepository<GlassThickness> {

    boolean existsByMm(int mm);
}
