package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.GlassThickness;
import java.util.Optional;

/** Acceso a los espesores de vidrio del configurador. */
public interface GlassThicknessRepository extends GlassOptionRepository<GlassThickness> {

    boolean existsByMm(int mm);

    /** Opción activa con ese identificador; la usan las cotizaciones para validar lo que eligió el cliente. */
    Optional<GlassThickness> findByMmAndActiveTrue(int mm);
}
