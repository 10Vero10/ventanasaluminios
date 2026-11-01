-- Catálogo de productos (con especificaciones y características ordenadas) y opciones del configurador de vidrio.

CREATE TABLE product (
    id               BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    position         INTEGER       NOT NULL,
    category         VARCHAR(20)   NOT NULL
        CHECK (category IN ('VENTANAS', 'PUERTAS', 'PORTONES', 'DIVISIONES')),
    category_number  VARCHAR(4),
    title            VARCHAR(80)   NOT NULL,
    subtitle         VARCHAR(120)  NOT NULL,
    description      VARCHAR(600)  NOT NULL,
    image_url        VARCHAR(2048),
    badge            VARCHAR(60),
    cta_text         VARCHAR(60),
    active           BOOLEAN       NOT NULL,
    -- Bloqueo optimista: dos ediciones cruzadas del mismo producto no pueden mezclar sus listas.
    version          BIGINT        NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_product_position UNIQUE (position) DEFERRABLE INITIALLY DEFERRED
);

CREATE INDEX ix_product_category ON product (category, position);

-- Listas ordenadas del producto: la posición es parte de la llave y la maneja Hibernate (@OrderColumn).
CREATE TABLE product_spec (
    product_id  BIGINT        NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    position    INTEGER       NOT NULL,
    label       VARCHAR(60)   NOT NULL,
    value       VARCHAR(150)  NOT NULL,
    PRIMARY KEY (product_id, position)
);

CREATE TABLE product_feature (
    product_id  BIGINT        NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    position    INTEGER       NOT NULL,
    text        VARCHAR(150)  NOT NULL,
    PRIMARY KEY (product_id, position)
);

-- Opciones del configurador. Se desactivan en lugar de borrarse y su código no cambia, porque las cotizaciones
-- guardan el código. is_default marca la opción inicial del configurador: como máximo una por tabla, verificado
-- al confirmar (diferible) para poder cambiar de opción por defecto dentro de una misma transacción.

CREATE TABLE glass_type (
    id          BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    position    INTEGER      NOT NULL,
    code        VARCHAR(30)  NOT NULL UNIQUE,
    name        VARCHAR(60)  NOT NULL,
    active      BOOLEAN      NOT NULL,
    is_default  BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_glass_type_position UNIQUE (position) DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT uq_glass_type_default EXCLUDE USING btree (is_default WITH =) WHERE (is_default)
        DEFERRABLE INITIALLY DEFERRED
);

CREATE TABLE glass_color (
    id          BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    position    INTEGER      NOT NULL,
    code        VARCHAR(30)  NOT NULL UNIQUE,
    name        VARCHAR(60)  NOT NULL,
    hex         VARCHAR(7)   NOT NULL CHECK (hex ~ '^#[0-9A-Fa-f]{6}$'),
    reflective  BOOLEAN      NOT NULL,
    note        VARCHAR(200),
    active      BOOLEAN      NOT NULL,
    is_default  BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_glass_color_position UNIQUE (position) DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT uq_glass_color_default EXCLUDE USING btree (is_default WITH =) WHERE (is_default)
        DEFERRABLE INITIALLY DEFERRED
);

CREATE TABLE glass_thickness (
    id          BIGINT   GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    position    INTEGER  NOT NULL,
    mm          INTEGER  NOT NULL UNIQUE CHECK (mm BETWEEN 1 AND 50),
    active      BOOLEAN  NOT NULL,
    is_default  BOOLEAN  NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_glass_thickness_position UNIQUE (position) DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT uq_glass_thickness_default EXCLUDE USING btree (is_default WITH =) WHERE (is_default)
        DEFERRABLE INITIALLY DEFERRED
);
