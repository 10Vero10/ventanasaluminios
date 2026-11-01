# 03 — Contrato de la API

Rutas, DTO y códigos de error acordados antes de programar. El frontend trabaja contra este contrato; si una ruta cambia, se actualiza aquí en el mismo PR. El detalle de cada DTO se completa al implementar cada módulo.

## Reglas generales

- Todo bajo `/api/v1`. Administración bajo `/api/v1/admin/**`, solo rol `ADMIN`.
- Campos JSON en inglés y `camelCase`. Textos para el usuario en español.
- Errores con un formato único: `{ "error": "mensaje en español", "code": "CODIGO" }`.

Los códigos están en `ErrorCode` (backend), que es la fuente de verdad de esta tabla.

| HTTP | `code` | Cuándo |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Un campo no cumple las reglas (el mensaje dice cuál) |
| 400 | `MALFORMED_REQUEST` | El JSON no se puede leer |
| 400 | `INVALID_PARAMETER` | Falta un parámetro o tiene formato inválido (por ejemplo, una línea inexistente) |
| 400 | `REORDER_MISMATCH` | El nuevo orden no trae exactamente los ids actuales |
| 400 | `EMPTY_FILE`, `INVALID_FILE_TYPE` | Imagen vacía, o que no es JPG, PNG ni WebP |
| 400 | `BAD_REQUEST` | Otra solicitud inválida |
| 401 | — | No autenticado o sesión vencida (paso 4) |
| 403 | — | Sin permiso, o cotización con el interruptor apagado (pasos 3 y 4) |
| 404 | `NOT_FOUND` | Recurso inexistente |
| 405 | `METHOD_NOT_ALLOWED` | Método no permitido en la ruta |
| 406 | — | El cliente no acepta JSON (respuesta sin cuerpo) |
| 409 | `CONFLICT` | Dos cambios simultáneos chocaron (por ejemplo, la misma posición); se pide actualizar e intentar de nuevo |
| 413 | `FILE_TOO_LARGE` | Imagen más grande que el límite configurado (5 MB) |
| 415 | `UNSUPPORTED_MEDIA_TYPE` | Tipo de contenido no soportado |
| 429 | — | Límite de solicitudes por IP (pasos 3 y 4) |
| 500 | `INTERNAL_ERROR` | Error inesperado; mensaje genérico, sin detalles técnicos |

## Públicos

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/health` | Estado de la API: `{ "status": "ok" }` |
| `GET` | `/site` | Configuración pública y líneas destacadas |
| `GET` | `/products?category=` | Productos activos, opcionalmente por categoría |
| `GET` | `/products/{id}` | Detalle de un producto |
| `GET` | `/glass-options` | Tipos, colores y espesores activos |
| `GET` | `/gallery?category=` | Galería de trabajos, opcionalmente por línea |
| `POST` | `/quotes` | Crear una cotización (reglas en briefing §3.6) |
| `GET` | `/media/{archivo}` | Imagen subida (fuera de `/api`) |

## Autenticación

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/auth/login` | Deja el JWT en cookie httpOnly |
| `POST` | `/auth/logout` | Invalida la cookie |
| `GET` | `/auth/me` | Usuario actual |
| `POST` | `/auth/change-password` | Cambio de contraseña (obligatorio en el primer ingreso) |

## Administración (`ADMIN`)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` · `PUT` | `/admin/settings` | Configuración del sitio |
| CRUD | `/admin/featured-lines[/{id}]` | Líneas destacadas |
| `PUT` | `/admin/featured-lines/order` | Reordenar |
| `GET` · `POST` · `PUT` · `DELETE` | `/admin/gallery[/{id}]` | Galería |
| `PUT` | `/admin/gallery/order` | Reordenar |
| CRUD | `/admin/products[/{id}]` | Productos con specs y características |
| `POST` | `/admin/products/{id}/duplicate` | Duplicar |
| `PUT` | `/admin/products/order` | Reordenar |
| CRUD | `/admin/glass-types`, `/admin/glass-colors`, `/admin/glass-thicknesses` | Opciones de vidrio |
| `GET` | `/admin/quotes?status=&page=&size=&sort=` | Bandeja paginada |
| `GET` | `/admin/quotes/{id}` | Detalle con historial de estados |
| `PATCH` | `/admin/quotes/{id}/status` | Cambiar estado |
| `DELETE` | `/admin/quotes/{id}` | Borrado lógico |
| `GET` | `/admin/dashboard` | `{ products, galleryImages, newQuotes }` |
| `POST` | `/admin/media` | Subir imagen; devuelve `{ url }` |
| `GET` · `POST` | `/admin/backup` | Exportar / importar respaldo |

## Seguridad y límites

- JWT en cookie `httpOnly`, `Secure`, `SameSite=Lax`, más token CSRF de Spring (`CookieCsrfTokenRepository`; axios envía `X-XSRF-TOKEN`).
- Límites por IP con filtro propio en memoria: 10 cotizaciones por hora y 5 intentos de login por minuto.
- Imágenes subidas: máximo 5 MB; solo JPG, PNG y WebP.

## DTO implementados

### Paso 1, settings

| DTO | Campos |
|---|---|
| `PublicSiteResponse` | `heroTitle`, `heroSubtitle`, `heroImageUrl`, `heroBadge`, `phone`, `email`, `whatsappNumber`, `experienceYears`, `quotingEnabled`, `faviconUrl`, `aboutParagraph1`, `aboutParagraph2`, `aboutHighlight`, `featuredLines[]` |
| `SiteSettingsRequest` | Los mismos campos sin `featuredLines`. Obligatorios: títulos, teléfono (con al menos un dígito), correo, años, `quotingEnabled` y textos de "Quiénes somos". `whatsappNumber` se guarda solo con dígitos; vacío se deriva del teléfono; el resultado debe tener entre 7 y 20 dígitos |
| `SiteSettingsResponse` | Los de `SiteSettingsRequest` + `updatedAt` |
| `FeaturedLineRequest` | `number`, `title`, `categoryTag`, `description`, `imageUrl`, `linkText`, `targetCategory` |
| `FeaturedLineResponse` | `id`, `position` + los de la solicitud |
| `GalleryImageRequest` | `title` (opcional, por defecto "Imagen"), `url`, `category` (opcional) |
| `GalleryImageResponse` | `id`, `position`, `title`, `url`, `category` |
| `ReorderRequest` | `ids[]`: todos los ids existentes, cada uno una vez |
| `MediaUploadResponse` | `url` |
| `ErrorResponse` | `error`, `code` |

Las URLs de imagen aceptan `http(s)://…` o rutas del mismo sitio (`/media/…`, `/favicon.svg`); nunca `data:`, `javascript:` ni rutas que el navegador lea como otro dominio (`//host`, `/\host`).
