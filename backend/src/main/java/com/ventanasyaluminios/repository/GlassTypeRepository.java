package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.GlassType;
import java.util.Optional;

/** Acceso a los tipos de vidrio del configurador. */
public interface GlassTypeRepository extends GlassOptionRepository<GlassType> {

    boolean existsByCode(String code);

    /** Opción activa con ese identificador; la usan las cotizaciones para validar lo que eligió el cliente. */
    Optional<GlassType> findByCodeAndActiveTrue(String code);
}
