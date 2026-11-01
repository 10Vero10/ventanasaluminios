# Backend

API REST de Ventanas y Aluminios SAS. Java 21, Spring Boot 3.5, Spring Data JPA, Flyway y PostgreSQL 16.

## Estructura

```
com.ventanasyaluminios
  ├─ settings/  catalog/  quote/  auth/
  │    ├─ controller/   REST, solo DTOs, @Valid
  │    ├─ service/      reglas de negocio y transacciones
  │    ├─ repository/   interfaces Spring Data JPA
  │    ├─ entity/       @Entity JPA
  │    ├─ dto/          records (request/response)
  │    └─ mapper/       conversión entidad ↔ DTO, a mano
  └─ common/            errores, configuración, seguridad, límite por IP
```

Las migraciones están en `src/main/resources/db/migration/` (`V1__descripcion.sql`, `V2__…`). Una migración ya ejecutada nunca se edita. Hibernate corre con `ddl-auto=validate`.

## Variables de entorno

| Variable | Por defecto | Uso |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/ventanas` | Conexión JDBC |
| `DB_USER` | `ventanas` | Usuario de la base |
| `DB_PASSWORD` | `ventanas` | Clave de la base |
| `SERVER_PORT` | `8080` | Puerto HTTP |

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

El resto de endpoints se documenta aquí a medida que se implementa cada módulo. La lista completa planeada está en [docs/decisiones.md](../docs/decisiones.md).
