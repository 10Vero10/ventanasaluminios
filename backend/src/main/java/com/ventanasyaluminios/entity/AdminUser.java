package com.ventanasyaluminios.entity;

import com.ventanasyaluminios.entity.enums.AdminRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;

/**
 * Usuario del panel de administración. La contraseña se guarda con BCrypt. {@code tokenVersion} va dentro de cada JWT:
 * al subirla (cambio de contraseña o cierre de sesión) todas las sesiones anteriores dejan de valer.
 */
@Entity
@Table(name = "admin_user")
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminRole role;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Para JPA. */
    protected AdminUser() {
    }

    /** Administrador nuevo y activo; el correo se guarda en minúsculas. */
    public AdminUser(String email, String passwordHash, boolean mustChangePassword, Instant createdAt) {
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.passwordHash = passwordHash;
        this.role = AdminRole.ADMIN;
        this.active = true;
        this.mustChangePassword = mustChangePassword;
        this.createdAt = createdAt;
    }

    /** Nueva contraseña: ya no se exige cambiarla y las sesiones anteriores quedan invalidadas. */
    public void changePassword(String newPasswordHash) {
        passwordHash = newPasswordHash;
        mustChangePassword = false;
        tokenVersion++;
    }

    /** Cierra todas las sesiones abiertas de este administrador. */
    public void revokeSessions() {
        tokenVersion++;
    }

    public void recordLogin(Instant now) {
        lastLoginAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AdminRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
