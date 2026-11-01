package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.AdminUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a los usuarios del panel. */
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    /** El correo se guarda en minúsculas; quien llama debe pasarlo igual. */
    Optional<AdminUser> findByEmail(String email);
}
