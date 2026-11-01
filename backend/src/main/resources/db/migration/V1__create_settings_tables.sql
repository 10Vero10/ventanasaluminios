-- Configuración del sitio (una sola fila), líneas destacadas de la portada y galería de trabajos.

CREATE TABLE site_settings (
    id                INTEGER       PRIMARY KEY CHECK (id = 1),
    hero_title        VARCHAR(120)  NOT NULL,
    hero_subtitle     VARCHAR(300)  NOT NULL,
    hero_image_url    VARCHAR(2048),
    hero_badge        VARCHAR(80),
    phone             VARCHAR(40)   NOT NULL,
    email             VARCHAR(254)  NOT NULL,
    whatsapp_number   VARCHAR(20)   NOT NULL,
    experience_years  INTEGER       NOT NULL CHECK (experience_years BETWEEN 0 AND 150),
    quoting_enabled   BOOLEAN       NOT NULL,
    favicon_url       VARCHAR(2048),
    about_paragraph1  VARCHAR(2000) NOT NULL,
    about_paragraph2  VARCHAR(2000) NOT NULL,
    about_highlight   VARCHAR(1000) NOT NULL,
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- position es única pero diferible: al reordenar, las posiciones se intercambian dentro de una
-- transacción y la unicidad se verifica solo al confirmar.
CREATE TABLE featured_line (
    id               BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    position         INTEGER       NOT NULL,
    number           VARCHAR(4)    NOT NULL,
    title            VARCHAR(80)   NOT NULL,
    category_tag     VARCHAR(80)   NOT NULL,
    description      VARCHAR(400)  NOT NULL,
    image_url        VARCHAR(2048),
    link_text        VARCHAR(40)   NOT NULL,
    target_category  VARCHAR(20)   NOT NULL
        CHECK (target_category IN ('VENTANAS', 'PUERTAS', 'PORTONES', 'DIVISIONES')),
    CONSTRAINT uq_featured_line_position UNIQUE (position) DEFERRABLE INITIALLY DEFERRED
);

-- category es opcional: una imagen sin línea solo aparece en el filtro "Todos".
CREATE TABLE gallery_image (
    id          BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    position    INTEGER       NOT NULL,
    title       VARCHAR(120)  NOT NULL,
    url         VARCHAR(2048) NOT NULL,
    category    VARCHAR(20)
        CHECK (category IN ('VENTANAS', 'PUERTAS', 'PORTONES', 'DIVISIONES')),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_gallery_image_position UNIQUE (position) DEFERRABLE INITIALLY DEFERRED
);

CREATE INDEX ix_gallery_image_category ON gallery_image (category, position);
