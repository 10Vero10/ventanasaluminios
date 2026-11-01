# CLAUDE.md

Sitio web y panel de administración de Ventanas y Aluminios SAS (Colombia). Los requisitos completos están en `docs/briefing.md`; las decisiones tomadas después están en `docs/decisiones.md`, que manda cuando hay diferencia.

## Reglas de trabajo

- Hablar con la usuaria en español. Identificadores del código en inglés; textos de la interfaz, mensajes de error de la API, documentación y commits en español.
- Avanzar un paso (fase) a la vez, según `README.md` → Estado. Un paso está terminado cuando compila, las pruebas pasan y `docker compose up` funciona.
- Preguntar antes de asumir algo ambiguo. No agregar librerías al backend sin consultar.
- Commits pequeños y frecuentes, con push después de cada cambio importante (`git push -u origin <rama>`).

## Stack

- `backend/`: Java 21, Spring Boot 3.5, Spring Web, Spring Data JPA, Spring Security + JWT (paso 4), Bean Validation, Flyway, PostgreSQL 16, Maven (`./mvnw`). Pruebas: JUnit 5, Mockito para services, Testcontainers para repositories e integración.
- `frontend/`: React 19, Vite, TypeScript strict (fijado en 6.0 hasta que typescript-eslint soporte la 7), Tailwind CSS v4, React Router, shadcn/ui (base Radix), ESLint.
- `docker-compose.yml`: `db` (Postgres), `backend`, `web` (nginx sirve el frontend y reenvía `/api` y `/media`), `pgadmin` opcional (perfil `tools`).

## Convenciones del backend

- Paquetes por capa bajo `com.ventanasyaluminios`: `config`, `controller`, `dto`, `entity`, `exception`, `mapper`, `repository`, `security`, `service`.
- Los controllers solo reciben y devuelven DTOs (`record` de Java) y validan con `@Valid`; nunca exponen entidades.
- Mappers entidad ↔ DTO escritos a mano, una clase por módulo. Sin Lombok.
- Los errores pasan por un `@RestControllerAdvice` global que responde `{ "error": "<mensaje en español>", "code": "<CODIGO>" }`.
- API bajo `/api/v1`; administración bajo `/api/v1/admin/**` con rol `ADMIN`.
- El esquema solo cambia con migraciones nuevas de Flyway en `backend/src/main/resources/db/migration/` (`V<n>__descripcion.sql`). Una migración ejecutada nunca se edita. Hibernate corre con `ddl-auto=validate`. Los datos iniciales también son migraciones.
- Configuración tipada con `@ConfigurationProperties`, validada al arrancar. Al correr localmente se lee el `.env` de la raíz (ver `application.yml`).

## Convenciones del frontend

- Estilo visual: tokens de `apple-style-reference` (en `src/index.css`) y movimiento de `apple-design`. No se conserva nada del diseño del sitio viejo.
- antislop activo en modo **durante** para este proyecto (elegido por la usuaria): aplicar sus reglas al construir y entregar cada vista con su reporte del Delivery Gate.
- Las afirmaciones de `docs/briefing.md` §6.3 están confirmadas como reales por la empresa.
- Las reglas del configurador de vidrio (briefing §3.5) se implementan en backend y frontend y se prueban con los mismos casos de ejemplo.

## Comandos

| Tarea | Comando |
|---|---|
| Todo el sistema | `docker compose up -d --build` (sitio en http://localhost:8080) |
| Solo la base | `docker compose up -d db` |
| Backend: correr / probar | `cd backend` y luego `./mvnw spring-boot:run` / `./mvnw verify` (requiere Docker) |
| Frontend: dev / lint / build | `npm run dev` / `npm run lint` / `npm run build` (desde la raíz o `frontend/`) |

## Referencia

El sitio viejo (`10Vero10/ventanasyaluminios`) es solo referencia de reglas de negocio y textos, nunca de estructura. Archivos útiles: `src/utils/blueprint.ts` (+ pruebas), `server/quotes.ts` (+ pruebas), `src/data/initialData.ts`.
