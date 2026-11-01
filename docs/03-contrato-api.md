# 03 — Contrato de la API

Rutas, DTO y códigos de error acordados antes de programar. El frontend trabaja contra este contrato; si una ruta cambia, se actualiza aquí en el mismo PR. El detalle de cada DTO se completa al implementar cada módulo.

## Reglas generales

- Todo bajo `/api/v1`. Administración bajo `/api/v1/admin/**`, solo rol `ADMIN`.
- Campos JSON en inglés y `camelCase`. Textos para el usuario en español.
- Errores con un formato único: `{ "error": "mensaje en español", "code": "CODIGO" }`.

| HTTP | Cuándo |
|---|---|
| 400 | Validación (`VALIDATION_ERROR`) |
| 401 | No autenticado o sesión vencida |
| 403 | Sin permiso, o cotización con el interruptor apagado |
| 404 | Recurso inexistente |
| 409 | Conflicto (por ejemplo, código de vidrio repetido) |
| 429 | Límite de solicitudes por IP |
| 500 | Error inesperado (mensaje genérico, sin detalles técnicos) |

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
