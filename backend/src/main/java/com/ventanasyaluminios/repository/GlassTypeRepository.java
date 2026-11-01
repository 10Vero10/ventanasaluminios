package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.GlassType;

/** Acceso a los tipos de vidrio del configurador. */
public interface GlassTypeRepository extends GlassOptionRepository<GlassType> {

    boolean existsByCode(String code);
}
