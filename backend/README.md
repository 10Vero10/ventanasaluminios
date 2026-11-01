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

El resto de endpoints se documenta aquí a medida que se implementa cada módulo. El contrato completo está en [docs/03-contrato-api.md](../docs/03-contrato-api.md).
