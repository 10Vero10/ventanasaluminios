package com.ventanasyaluminios.security;

import com.ventanasyaluminios.config.AppProperties;
import com.ventanasyaluminios.entity.AdminUser;
import com.ventanasyaluminios.repository.AdminUserRepository;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el primer administrador al arrancar si no existe ninguno, con el correo y la contraseña temporal de las
 * variables ADMIN_EMAIL y ADMIN_INITIAL_PASSWORD (ADR-005). Debe cambiar la contraseña en su primer ingreso. Si ya hay
 * administradores, no hace nada: cambiar las variables no altera usuarios existentes.
 */
@Component
public class InitialAdminCreator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAdminCreator.class);

    private final AdminUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties.Auth auth;
    private final Clock clock;

    public InitialAdminCreator(AdminUserRepository repository, PasswordEncoder passwordEncoder, AppProperties properties,
                               Clock clock) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.auth = properties.auth();
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        if (auth.initialAdminEmail() == null || auth.initialAdminEmail().isBlank()
                || auth.initialAdminPassword() == null || auth.initialAdminPassword().isBlank()) {
            log.warn("No hay administradores. Define ADMIN_EMAIL y ADMIN_INITIAL_PASSWORD para crear el primero.");
            return;
        }
        AdminUser admin = new AdminUser(auth.initialAdminEmail(), passwordEncoder.encode(auth.initialAdminPassword()),
                true, Instant.now(clock));
        repository.save(admin);
        log.info("Administrador inicial creado: {}. Debe cambiar la contraseña en su primer ingreso.", admin.getEmail());
    }
}
