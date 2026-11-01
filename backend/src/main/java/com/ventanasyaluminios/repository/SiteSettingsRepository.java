package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.SiteSettings;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a la única fila de configuración del sitio. */
public interface SiteSettingsRepository extends JpaRepository<SiteSettings, Short> {
}
