# 02 — Modelo de datos

Tablas de PostgreSQL 16. El esquema se crea solo con migraciones de Flyway (`backend/src/main/resources/db/migration/`); Hibernate corre con `ddl-auto=validate`. Si una tabla o relación cambia, se actualiza este archivo en el mismo PR y el diagrama de `docs/diagramas/`.

## Convenciones

- Llaves `BIGINT GENERATED ALWAYS AS IDENTITY`, salvo `quote`, que usa `UUID` porque su id se devuelve al público y no debe revelar cuántas cotizaciones hay.
- Enumeraciones como `VARCHAR` + `CHECK`; en Java, `@Enumerated(EnumType.STRING)` con los enums de `entity/enums`.
- Fechas `TIMESTAMPTZ` en UTC.
- Las posiciones únicas son `DEFERRABLE INITIALLY DEFERRED`: al reordenar, se intercambian dentro de una transacción y la unicidad se verifica al confirmar.
- Las opciones de vidrio se desactivan (`active = false`), no se borran, para no dejar cotizaciones con códigos huérfanos.

## Enumeraciones

| Enum | Valores |
|---|---|
| `ProductCategory` | `VENTANAS`, `PUERTAS`, `PORTONES`, `DIVISIONES` |
| `QuoteStatus` | `NUEVA`, `CONTACTADA`, `COTIZADA`, `CERRADA`, `DESCARTADA` |
| `AdminRole` | `ADMIN` |

## Tablas

| Tabla | Columnas | Notas |
|---|---|---|
| `site_settings` | `id SMALLINT` (`CHECK id = 1`), `hero_title`, `hero_subtitle`, `hero_image_url`, `hero_badge`, `phone`, `email`, `whatsapp_number`, `experience_years`, `quoting_enabled`, `favicon_url`, `about_paragraph1`, `about_paragraph2`, `about_highlight`, `updated_at` | Una sola fila |
| `featured_line` | `id`, `position` (única, diferible), `number`, `title`, `category_tag`, `description`, `image_url`, `link_text`, `target_category` | Tarjetas de la portada |
| `gallery_image` | `id`, `position` (única, diferible), `title`, `url`, `category` (nula), `created_at` | Galería de trabajos. `url` externa o `/media/{archivo}`. Sin línea solo aparece en "Todos". Índice `(category, position)` |
| `product` | `id`, `category`, `category_number`, `title`, `subtitle`, `description`, `image_url`, `badge`, `cta_text`, `position`, `active`, `created_at`, `updated_at` | Índice `(category, position)` |
| `product_spec` | `id`, `product_id` (FK, `ON DELETE CASCADE`), `position`, `label`, `value` | `UNIQUE(product_id, position)` |
| `product_feature` | `id`, `product_id` (FK, `ON DELETE CASCADE`), `position`, `text` | `UNIQUE(product_id, position)` |
| `glass_type` | `id`, `code` (único), `name`, `position`, `active` | |
| `glass_color` | `id`, `code` (único), `name`, `hex`, `reflective`, `note`, `position`, `active` | |
| `glass_thickness` | `id`, `mm` (único), `position`, `active` | |
| `quote` | `id UUID`, `legacy_id` (único, nulo), `created_at`, `updated_at`, `deleted_at`, `name`, `phone`, `product_type`, `width_cm`, `height_cm`, `panes`, `area_m2 NUMERIC(6,2)`, `glass_type_code`, `glass_color_code`, `thickness_mm`, `dimensions_text`, `message`, `status`, `source_ip` | Códigos del vidrio sin FK (copia del momento). Índice parcial `(status, created_at) WHERE deleted_at IS NULL`. `legacy_id` guarda el id de Redis en la migración del paso 6 |
| `quote_status_history` | `id`, `quote_id` (FK), `from_status`, `to_status`, `changed_at`, `changed_by` (FK `admin_user`) | Rastro de cada cambio de estado |
| `admin_user` | `id`, `email` (único), `password_hash`, `role`, `active`, `must_change_password`, `token_version`, `last_login_at`, `created_at` | `token_version` invalida los JWT al cambiar la contraseña |

## Relaciones

- `product` 1 — N `product_spec` y `product_feature` (se borran con el producto).
- `quote` 1 — N `quote_status_history`; `admin_user` 1 — N `quote_status_history`.
- `site_settings`, `featured_line`, `gallery_image` y las opciones de vidrio no tienen relaciones entre sí.

## Migraciones

| Versión | Contenido |
|---|---|
| `V1__create_settings_tables` | `site_settings`, `featured_line`, `gallery_image` |
| `V2__seed_settings` | Configuración inicial, 4 líneas destacadas y 5 imágenes (briefing §5) |
