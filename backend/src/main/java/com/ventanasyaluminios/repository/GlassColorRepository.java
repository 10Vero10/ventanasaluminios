package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.GlassColor;
import java.util.Optional;

/** Acceso a los colores de vidrio del configurador. */
public interface GlassColorRepository extends GlassOptionRepository<GlassColor> {

    boolean existsByCode(String code);

    /** Opción activa con ese identificador; la usan las cotizaciones para validar lo que eligió el cliente. */
    Optional<GlassColor> findByCodeAndActiveTrue(String code);
}
