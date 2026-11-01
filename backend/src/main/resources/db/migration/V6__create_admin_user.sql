-- Administradores del panel. El primero se crea al arrancar desde variables de entorno (ADR-005), nunca aquí.

CREATE TABLE admin_user (
    id                    BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    -- Siempre en minúsculas: el login compara sin distinguir mayúsculas.
    email                 VARCHAR(254)  NOT NULL UNIQUE CHECK (email = lower(email)),
    password_hash         VARCHAR(100)  NOT NULL,
    role                  VARCHAR(20)   NOT NULL CHECK (role IN ('ADMIN')),
    active                BOOLEAN       NOT NULL,
    must_change_password  BOOLEAN       NOT NULL,
    -- Sube al cambiar la contraseña o cerrar sesión: invalida todos los JWT emitidos antes.
    token_version         INTEGER       NOT NULL DEFAULT 0,
    last_login_at         TIMESTAMPTZ,
    created_at            TIMESTAMPTZ   NOT NULL
);

-- Quién cambió el estado de una cotización (nulo en los cambios anteriores a este paso).
ALTER TABLE quote_status_change
    ADD CONSTRAINT fk_quote_status_change_admin FOREIGN KEY (changed_by) REFERENCES admin_user (id) ON DELETE SET NULL;
