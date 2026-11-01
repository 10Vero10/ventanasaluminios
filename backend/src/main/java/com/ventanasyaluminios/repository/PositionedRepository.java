package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.Positioned;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

/** Consultas comunes a toda lista ordenable por el administrador. */
@NoRepositoryBean
public interface PositionedRepository<T extends Positioned> extends JpaRepository<T, Long> {

    List<T> findAllByOrderByPositionAsc();

    /** Última posición usada; 0 si la lista está vacía. Un elemento nuevo va en {@code max + 1}. */
    @Query("select coalesce(max(e.position), 0) from #{#entityName} e")
    int findMaxPosition();
}
