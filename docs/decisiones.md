# Decisiones del proyecto

Registro de lo acordado durante la reconstrucción. Complementa `briefing.md`; cuando hay diferencia, manda este archivo.

## Decisiones de producto (§6.2 del briefing)

| Tema | Decisión |
|---|---|
| Hojas del vidrio | El cliente elige el número de hojas (1 a 4). El configurador propone `max(1, ceil(ancho / 150))` como valor inicial. Se guarda en la cotización y aparece en el texto de medidas cuando es mayor que 1: `"… · Vidrio crudo transparente 6 mm · 3 hojas"`. |
| Color "Verde Frence" | Se mantiene ese nombre. |
| Galería | Es pública: página **"Galería de trabajos"** en `/trabajos`. Cada imagen muestra su título, pertenece a una línea (`VENTANAS`, `PUERTAS`, `PORTONES`, `DIVISIONES`) y se filtra por línea. |
| Etiqueta del catálogo | Se quita "GALERÍA DE TRABAJOS" de cada tarjeta. En su lugar, un enlace "Ver trabajos de esta línea" abre `/trabajos` filtrado. |
| Botones del catálogo | Portón: "Cotizar portón americano". Corredizas: "Cotizar ventana corrediza". |
| Despliegue | Un solo dominio. `docker compose` levanta nginx (sirve el frontend y reenvía `/api` y `/media`), el backend y PostgreSQL. El servidor de destino se define en el paso 6. |
| Primer administrador | Se crea al arrancar desde variables de entorno si no existe ninguno, con cambio de contraseña obligatorio en el primer ingreso. La contraseña nunca queda en el repositorio. |
| Estados de cotización | El administrador puede cambiar el estado libremente (incluso hacia atrás). Cada cambio queda en `quote_status_history`. |
| Respaldo | Formato nuevo, versión 1. No se importan respaldos del sitio viejo. |
| Afirmaciones de §6.3 | Confirmadas como reales: "Norma NSR", "Respuesta técnica en menos de 24 horas", "Instaladores certificados", "Lunes a Viernes 7:30 AM – 6:00 PM", "± 1.0 mm". |
| Imágenes iniciales | Las del sitio viejo eran de prueba. Se usan como provisionales y se reemplazan desde el panel. |

## Estructura del código

- Backend organizado **por capas** (no por módulo como proponía el briefing §2.4): `com.ventanasyaluminios.config`, `controller`, `dto`, `entity`, `exception`, `mapper`, `repository`, `security`, `service`. Sigue la estructura de referencia que dio la usuaria.
- Raíz del repo con `.github/` (CI), `.gitattributes`, `CLAUDE.md`, `docker-compose.yml`, `.env.example` y `package.json` con atajos al frontend.
- Frontend con shadcn/ui (base Radix) y ESLint. TypeScript fijado en 6.0 hasta que typescript-eslint soporte la 7.

## Frontend

- Estilo visual completamente nuevo basado en las skills `apple-style-reference` (paleta, tipografía, radios, superficies) y `apple-design` (animación fluida, botones, gestos). No se conserva nada del diseño anterior.
- `antislop` en modo **durante**: sus reglas se aplican mientras se construye y cada entrega lleva su reporte de verificación.

## Revisión y seguridad

- `security-audit` al cerrar los pasos 3 y 4 y antes del despliegue.
- `thermos` (lo invoca la usuaria con `/thermos`) al cerrar cada paso.

## Modelo de datos

Convenciones: llaves `BIGINT GENERATED ALWAYS AS IDENTITY` salvo `quote` (UUID); enums como `VARCHAR` + `CHECK` (`@Enumerated(STRING)` en Java); fechas `TIMESTAMPTZ` en UTC.

| Tabla | Columnas | Notas |
|---|---|---|
| `site_settings` | `id SMALLINT` (`CHECK id = 1`), `hero_title`, `hero_subtitle`, `hero_image_url`, `hero_badge`, `phone`, `email`, `whatsapp_number`, `experience_years`, `quoting_enabled`, `favicon_url`, `about_paragraph1`, `about_paragraph2`, `about_highlight`, `updated_at` | Una sola fila |
| `featured_line` | `id`, `position` (única), `number`, `title`, `category_tag`, `description`, `image_url`, `link_text`, `target_category` | |
| `gallery_image` | `id`, `position`, `title`, `url`, `category`, `created_at` | `url` externa o `/media/{archivo}` |
| `product` | `id`, `category`, `category_number`, `title`, `subtitle`, `description`, `image_url`, `badge`, `cta_text`, `position`, `active`, `created_at`, `updated_at` | Índice `(category, position)` |
| `product_spec` | `id`, `product_id` (FK, `ON DELETE CASCADE`), `position`, `label`, `value` | `UNIQUE(product_id, position)` |
| `product_feature` | `id`, `product_id` (FK, `ON DELETE CASCADE`), `position`, `text` | `UNIQUE(product_id, position)` |
| `glass_type` | `id`, `code` (único), `name`, `position`, `active` | Se desactiva, no se borra |
| `glass_color` | `id`, `code` (único), `name`, `hex`, `reflective`, `note`, `position`, `active` | Se desactiva, no se borra |
| `glass_thickness` | `id`, `mm` (único), `position`, `active` | Se desactiva, no se borra |
| `quote` | `id UUID`, `legacy_id` (único, nulo), `created_at`, `updated_at`, `deleted_at`, `name`, `phone`, `product_type`, `width_cm`, `height_cm`, `panes`, `area_m2 NUMERIC(6,2)`, `glass_type_code`, `glass_color_code`, `thickness_mm`, `dimensions_text`, `message`, `status`, `source_ip` | Códigos del vidrio sin FK (copia del momento). Índice parcial `(status, created_at) WHERE deleted_at IS NULL` |
| `quote_status_history` | `id`, `quote_id` (FK), `from_status`, `to_status`, `changed_at`, `changed_by` (FK `admin_user`) | |
| `admin_user` | `id`, `email` (único), `password_hash`, `role`, `active`, `must_change_password`, `token_version`, `last_login_at`, `created_at` | `token_version` invalida los JWT al cambiar la contraseña |

## Endpoints

Todo bajo `/api/v1`. Errores: `{ "error": "mensaje en español", "code": "CODIGO" }`.

**Públicos:** `GET /site` · `GET /products?category=` · `GET /products/{id}` · `GET /glass-options` · `GET /gallery?category=` · `POST /quotes` · `GET /health`. Además `GET /media/{archivo}` (fuera de `/api`).

**Autenticación:** `POST /auth/login` · `POST /auth/logout` · `GET /auth/me` · `POST /auth/change-password`.

**Administración (`ADMIN`):** `GET|PUT /admin/settings` · CRUD `/admin/featured-lines` + `PUT /admin/featured-lines/order` · `GET|POST|PUT|DELETE /admin/gallery` + `PUT /admin/gallery/order` · CRUD `/admin/products` + `POST /admin/products/{id}/duplicate` + `PUT /admin/products/order` · CRUD `/admin/glass-types`, `/admin/glass-colors`, `/admin/glass-thicknesses` · `GET /admin/quotes?status=&page=&size=&sort=` · `GET /admin/quotes/{id}` · `PATCH /admin/quotes/{id}/status` · `DELETE /admin/quotes/{id}` (lógico) · `GET /admin/dashboard` · `POST /admin/media` · `GET|POST /admin/backup`.

**Límites por IP (filtro propio, en memoria):** 10 cotizaciones por hora; 5 intentos de login por minuto.

**Imágenes subidas:** máximo 5 MB; solo JPG, PNG y WebP.

**Cookies:** JWT en cookie `httpOnly`, `Secure`, `SameSite=Lax`, más token CSRF de Spring (`CookieCsrfTokenRepository`).
