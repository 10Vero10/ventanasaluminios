package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.GlassOption;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

/** Consultas comunes a los tipos, colores y espesores del configurador. */
@NoRepositoryBean
public interface GlassOptionRepository<T extends GlassOption> extends PositionedRepository<T> {

    List<T> findByActiveTrueOrderByPositionAsc();

    /** La opción por defecto actual, si hay una. */
    Optional<T> findByDefaultOptionTrue();
}
