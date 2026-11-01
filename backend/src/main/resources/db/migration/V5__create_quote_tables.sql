-- Cotizaciones recibidas desde el formulario público y su historial de estados.

CREATE TABLE quote (
    id                UUID           PRIMARY KEY,
    -- Id original en Upstash Redis; solo lo tienen las cotizaciones migradas en el paso 6.
    legacy_id         VARCHAR(100)   UNIQUE,
    created_at        TIMESTAMPTZ    NOT NULL,
    updated_at        TIMESTAMPTZ    NOT NULL,
    -- Borrado lógico: una cotización eliminada desde el panel conserva su fila.
    deleted_at        TIMESTAMPTZ,
    name              VARCHAR(100)   NOT NULL,
    phone             VARCHAR(40)    NOT NULL,
    product_type      VARCHAR(120),
    -- Datos del configurador: todos o ninguno.
    width_cm          INTEGER        CHECK (width_cm BETWEEN 1 AND 600),
    height_cm         INTEGER        CHECK (height_cm BETWEEN 1 AND 260),
    panes             INTEGER        CHECK (panes BETWEEN 1 AND 4),
    area_m2           NUMERIC(6, 2),
    -- Códigos copiados en el momento: sin llave foránea, para que la cotización no cambie si la opción cambia.
    glass_type_code   VARCHAR(30),
    glass_color_code  VARCHAR(30),
    thickness_mm      INTEGER,
    dimensions_text   VARCHAR(200),
    message           VARCHAR(2000),
    status            VARCHAR(20)    NOT NULL
        CHECK (status IN ('NUEVA', 'CONTACTADA', 'COTIZADA', 'CERRADA', 'DESCARTADA')),
    source_ip         VARCHAR(45),
    CONSTRAINT ck_quote_configurator_all_or_none CHECK (
        (width_cm IS NULL AND height_cm IS NULL AND panes IS NULL AND area_m2 IS NULL
            AND glass_type_code IS NULL AND glass_color_code IS NULL AND thickness_mm IS NULL)
        OR (width_cm IS NOT NULL AND height_cm IS NOT NULL AND panes IS NOT NULL AND area_m2 IS NOT NULL
            AND glass_type_code IS NOT NULL AND glass_color_code IS NOT NULL AND thickness_mm IS NOT NULL))
);

-- Bandeja del panel: cotizaciones vigentes por estado y fecha.
CREATE INDEX ix_quote_status_created ON quote (status, created_at) WHERE deleted_at IS NULL;
CREATE INDEX ix_quote_created ON quote (created_at) WHERE deleted_at IS NULL;

-- changed_by apuntará a admin_user cuando exista (paso 4); hasta entonces queda nulo.
CREATE TABLE quote_status_change (
    id           BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    quote_id     UUID          NOT NULL REFERENCES quote (id) ON DELETE CASCADE,
    from_status  VARCHAR(20)   NOT NULL,
    to_status    VARCHAR(20)   NOT NULL,
    changed_at   TIMESTAMPTZ   NOT NULL,
    changed_by   BIGINT
);

CREATE INDEX ix_quote_status_change_quote ON quote_status_change (quote_id, changed_at);
