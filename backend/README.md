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
| `POST` | `/api/v1/admin/media` | Panel | Subir imagen (`multipart/form-data`, campo `file`): JPG, PNG o WebP de máximo 5 MB. Responde `201 { "url": "/media/…" }` |

> Las rutas del panel (`/api/v1/admin/**`) quedan abiertas hasta el paso 4, cuando se protegen con login y rol `ADMIN`. No desplegar antes de eso.

Los códigos de error y su estado HTTP están en `exception/ErrorCode.java`; la tabla completa está en [docs/03-contrato-api.md](../docs/03-contrato-api.md).

El resto de endpoints se documenta aquí a medida que se implementa cada módulo. El contrato completo está en [docs/03-contrato-api.md](../docs/03-contrato-api.md).
