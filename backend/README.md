# Backend

API REST de Ventanas y Aluminios SAS. Java 21, Spring Boot 3.5, Spring Data JPA, Flyway y PostgreSQL 16.

## Estructura

```
com.ventanasyaluminios
  ├─ config/          @ConfigurationProperties, CORS, beans compartidos
  ├─ controller/      REST bajo /api/v1, solo DTOs, @Valid
  ├─ dto/
  │    ├─ request/    records de entrada
  │    └─ response/   records de salida
  ├─ entity/          @Entity JPA (sin Lombok)
  │    └─ enums/      enumeraciones guardadas como texto
  ├─ exception/       excepciones de negocio y @RestControllerAdvice
  ├─ mapper/          entidad ↔ DTO, a mano
  ├─ repository/      interfaces Spring Data JPA
  ├─ security/        Spring Security, JWT en cookie, límite por IP
  └─ service/         interfaces de los servicios
       └─ impl/       implementaciones
```

Las migraciones están en `src/main/resources/db/migration/` (`V1__descripcion.sql`, `V2__…`). Una migración ya ejecutada nunca se edita. Hibernate corre con `ddl-auto=validate`.

## Variables de entorno

| Variable | Por defecto | Uso |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/ventanas` | Conexión JDBC |
| `DB_USER` | `ventanas` | Usuario de la base |
| `DB_PASSWORD` | `ventanas` | Clave de la base |
| `SERVER_PORT` | `8080` | Puerto HTTP |
| `MEDIA_UPLOAD_DIR` | `uploads` | Carpeta de imágenes subidas (en Docker, `/app/uploads` en el volumen `uploads`) |
| `CORS_ALLOWED_ORIGINS` | vacío | Orígenes CORS exactos separados por coma (no admite `*`). Vacío detrás de nginx, porque todo está en un solo dominio |
| `JWT_SECRET` | **obligatoria** | Clave para firmar las sesiones del panel (mínimo 32 caracteres). Sin ella el backend no arranca |
| `AUTH_SESSION_DURATION` | `8h` | Duración de una sesión del panel |
| `AUTH_COOKIE_SECURE` | `true` | Cookie de sesión solo por HTTPS (`localhost` cuenta como seguro) |
| `ADMIN_EMAIL`, `ADMIN_INITIAL_PASSWORD` | vacías | Primer administrador; se crea al arrancar si no existe ninguno y debe cambiar la contraseña en su primer ingreso |

Si no se definen, `DB_URL`, `DB_USER` y `DB_PASSWORD` se arman con `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER` y `POSTGRES_PASSWORD` del `.env` de la raíz.

## Comandos

```bash
./mvnw verify            # compila y corre las pruebas (requiere Docker)
./mvnw spring-boot:run   # arranca la API
```

## Endpoints

Todos bajo `/api/v1`. Los errores responden `{ "error": "mensaje en español", "code": "CODIGO" }`.

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/health` | Público | Estado de la API: `{ "status": "ok" }` |
| `POST` | `/api/v1/auth/login` | Público | `{ email, password }` → deja la cookie de sesión `vya_session` y devuelve el administrador. Máximo 5 intentos por minuto por IP |
| `GET` | `/api/v1/auth/me` | Sesión | Administrador actual |
| `POST` | `/api/v1/auth/change-password` | Sesión | `{ currentPassword, newPassword }`; invalida las sesiones anteriores y deja una nueva |
| `POST` | `/api/v1/auth/logout` | Sesión | Borra la cookie e invalida todas las sesiones del administrador |
| `GET` | `/api/v1/site` | Público | Configuración del sitio y líneas destacadas en orden |
| `GET` | `/api/v1/gallery?category=` | Público | Galería de trabajos; `category` opcional (`VENTANAS`, `PUERTAS`, `PORTONES`, `DIVISIONES`) |
| `GET` | `/media/{archivo}` | Público | Imagen subida desde el panel |
| `GET` · `PUT` | `/api/v1/admin/settings` | Panel | Ver y editar la configuración (`SiteSettingsRequest`) |
| `GET` · `POST` | `/api/v1/admin/featured-lines` | Panel | Listar y crear líneas destacadas (`FeaturedLineRequest`); la nueva queda al final |
| `PUT` · `DELETE` | `/api/v1/admin/featured-lines/{id}` | Panel | Editar y eliminar |
| `PUT` | `/api/v1/admin/featured-lines/order` | Panel | Reordenar: `{ "ids": [3, 1, 2, 4] }` con todos los ids |
| `GET` · `POST` | `/api/v1/admin/gallery` | Panel | Listar y agregar imágenes (`GalleryImageRequest`); sin título queda "Imagen" |
| `PUT` · `DELETE` | `/api/v1/admin/gallery/{id}` | Panel | Editar y eliminar |
| `PUT` | `/api/v1/admin/gallery/order` | Panel | Reordenar, igual que las líneas |
| `GET` | `/api/v1/products?category=` | Público | Productos activos en orden, opcionalmente por línea |
| `GET` | `/api/v1/products/{id}` | Público | Detalle de un producto activo (uno oculto responde 404) |
| `GET` | `/api/v1/glass-options` | Público | Tipos, colores y espesores activos, cada uno con su opción por defecto |
| `GET` · `POST` | `/api/v1/admin/products` | Panel | Listar todos (también ocultos) y crear (`ProductRequest`); el nuevo queda al final |
| `GET` · `PUT` · `DELETE` | `/api/v1/admin/products/{id}` | Panel | Ver, editar (la ficha técnica y las características se reemplazan en el orden enviado) y eliminar |
| `POST` | `/api/v1/admin/products/{id}/duplicate` | Panel | Copia "… (copia)" al final y oculta |
| `PUT` | `/api/v1/admin/products/order` | Panel | Reordenar el catálogo con todos los ids |
| `GET` | `/api/v1/admin/glass-options` | Panel | Todas las opciones de vidrio, también las inactivas |
| `POST` · `PUT` | `/api/v1/admin/glass-types[/{id}]` | Panel | Crear y editar tipos (`GlassTypeRequest`); sin DELETE, se desactivan |
| `POST` · `PUT` | `/api/v1/admin/glass-colors[/{id}]` | Panel | Crear y editar colores (`GlassColorRequest`) |
| `POST` · `PUT` | `/api/v1/admin/glass-thicknesses[/{id}]` | Panel | Crear y editar espesores (`GlassThicknessRequest`) |
| `PUT` | `/api/v1/admin/glass-{types,colors,thicknesses}/order` | Panel | Reordenar cada lista |
| `POST` | `/api/v1/quotes` | Público | Solicitud de cotización (`QuoteCreateRequest`). Responde `201 { "id" }`. Máximo 10 por hora por IP (429 `RATE_LIMITED`); 403 `QUOTING_DISABLED` si la cotización está apagada |
| `GET` | `/api/v1/admin/quotes?status=&page=&size=&sort=` | Panel | Bandeja paginada (`size` 1 a 100, por defecto 20; `sort` `createdAt,asc` por defecto o `createdAt,desc`) |
| `GET` | `/api/v1/admin/quotes/{id}` | Panel | Detalle con historial de estados (`QuoteDetailResponse`) |
| `PATCH` | `/api/v1/admin/quotes/{id}/status` | Panel | Cambiar estado libremente (`{ "status": "CONTACTADA" }`); queda en el historial |
| `DELETE` | `/api/v1/admin/quotes/{id}` | Panel | Borrado lógico |
| `GET` | `/api/v1/admin/dashboard` | Panel | `{ products, galleryImages, newQuotes }` |
| `GET` · `POST` | `/api/v1/admin/backup` | Panel | Exportar e importar el respaldo `{ version, exportedAt, content, products }` |
| `POST` | `/api/v1/admin/media` | Panel | Subir imagen (`multipart/form-data`, campo `file`): JPG, PNG o WebP de máximo 5 MB. Responde `201 { "url": "/media/…" }` |

**Acceso:** "Panel" exige una sesión de administrador con la contraseña temporal ya cambiada (rol `ADMIN`); "Sesión" exige solo haber ingresado. Todo `POST`, `PUT`, `PATCH` y `DELETE` con sesión exige además el token CSRF: el navegador recibe la cookie `XSRF-TOKEN` y debe devolverla en el encabezado `X-XSRF-TOKEN` (axios lo hace solo).

Los códigos de error y su estado HTTP están en `exception/ErrorCode.java`; la tabla completa está en [docs/03-contrato-api.md](../docs/03-contrato-api.md).

El resto de endpoints se documenta aquí a medida que se implementa cada módulo. El contrato completo está en [docs/03-contrato-api.md](../docs/03-contrato-api.md).
