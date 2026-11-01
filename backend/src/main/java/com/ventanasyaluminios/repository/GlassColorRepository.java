package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.GlassColor;

/** Acceso a los colores de vidrio del configurador. */
public interface GlassColorRepository extends GlassOptionRepository<GlassColor> {

    boolean existsByCode(String code);
}
