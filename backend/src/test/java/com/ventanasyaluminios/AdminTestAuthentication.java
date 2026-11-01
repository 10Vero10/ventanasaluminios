package com.ventanasyaluminios;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.ventanasyaluminios.security.AdminAuthenticationConverter;
import com.ventanasyaluminios.security.AuthenticatedAdmin;
import java.util.List;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Para pruebas de módulos del panel: cada solicitud de MockMvc va como un administrador con token CSRF. La seguridad
 * en sí (login, cookie, CSRF real, 401/403) se prueba aparte, sin esta configuración.
 */
@TestConfiguration(proxyBeanMethods = false)
public class AdminTestAuthentication {

    /** Sin id: el historial de cotizaciones queda sin autor, como antes del login. */
    public static final AuthenticatedAdmin ADMIN = new AuthenticatedAdmin(null, "admin@prueba.local", false);

    @Bean
    MockMvcBuilderCustomizer authenticateAsAdmin() {
        var token = new UsernamePasswordAuthenticationToken(ADMIN, null,
                List.of(new SimpleGrantedAuthority(AdminAuthenticationConverter.ROLE_ADMIN)));
        return builder -> builder.defaultRequest(MockMvcRequestBuilders.get("/").with(authentication(token)).with(csrf()));
    }
}
