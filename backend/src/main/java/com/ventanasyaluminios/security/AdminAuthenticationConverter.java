package com.ventanasyaluminios.security;

import com.ventanasyaluminios.entity.AdminUser;
import com.ventanasyaluminios.repository.AdminUserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

/**
 * Convierte un JWT ya verificado (firma, emisor y vencimiento) en el administrador de la solicitud. Consulta la base
 * en cada solicitud: si el administrador fue desactivado o su versión de token subió (cambio de contraseña o cierre
 * de sesión), el token deja de valer aunque no haya vencido.
 */
public class AdminAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    /** Rol con acceso al panel. */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /** Rol temporal: solo puede ver su usuario, cambiar la contraseña y cerrar sesión. */
    public static final String ROLE_PASSWORD_CHANGE_REQUIRED = "ROLE_PASSWORD_CHANGE_REQUIRED";

    private final AdminUserRepository repository;

    public AdminAuthenticationConverter(AdminUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        AdminUser admin = parseId(jwt.getSubject())
                .flatMap(repository::findById)
                .filter(AdminUser::isActive)
                .filter(user -> versionOf(jwt) == user.getTokenVersion())
                .orElseThrow(() -> new InvalidBearerTokenException("Sesión revocada o inexistente"));
        String role = admin.isMustChangePassword() ? ROLE_PASSWORD_CHANGE_REQUIRED : ROLE_ADMIN;
        AuthenticatedAdmin principal = new AuthenticatedAdmin(admin.getId(), admin.getEmail(),
                admin.isMustChangePassword());
        return new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority(role)));
    }

    private static Optional<Long> parseId(String subject) {
        try {
            return Optional.of(Long.parseLong(subject));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static long versionOf(Jwt jwt) {
        Object claim = jwt.getClaims().get(SessionTokens.VERSION_CLAIM);
        return claim instanceof Number number ? number.longValue() : -1;
    }
}
