# CLAUDE.md

Sitio web y panel de administración de Ventanas y Aluminios SAS (Colombia). Los requisitos completos están en `docs/01-briefing.md`; las decisiones tomadas después están en `docs/04-decisiones-tecnicas.md` (ADR), que manda cuando hay diferencia. Estructura y flujo tomados del proyecto de referencia SIERAP (github.com/Oskarsito7/SIERAP).

## Reglas de trabajo

- Hablar con la usuaria en español. Identificadores del código en inglés; textos de la interfaz, mensajes de error de la API, documentación y commits en español.
- Avanzar un paso (fase) a la vez, según `README.md` → Estado. Un paso está terminado cuando compila, las pruebas pasan y `docker compose up` funciona.
- Preguntar antes de asumir algo ambiguo. No agregar librerías al backend sin consultar.
- Ramas: `main` (entregado), `develop` (integración) y ramas temporales `feature/`, `fix/`, `docs/`, `chore/`, `refactor/`, `test/` que salen de `develop` y vuelven por PR. Nunca push directo a `main`. Detalle en `docs/05-plan-de-trabajo.md`.
- Commits pequeños y frecuentes con Conventional Commits y descripción en español (`feat(settings): ...`), con push después de cada cambio importante (`git push -u origin <rama>`).
- Cada PR cumple el checklist de `docs/05-plan-de-trabajo.md` §2.6.

## Stack

- `backend/`: Java 21, Spring Boot 3.5, Spring Web, Spring Data JPA, Spring Security + JWT (paso 4), Bean Validation, Flyway, PostgreSQL 16, Maven (`./mvnw`). Pruebas: JUnit 5, Mockito para services, Testcontainers para repositories e integración.
- `frontend/`: React 19, Vite, TypeScript strict (fijado en 6.0 hasta que typescript-eslint soporte la 7), Tailwind CSS v4, React Router, shadcn/ui (base Radix), ESLint.
- `Dockerfile` (raíz): imagen única que compila frontend y backend; el backend sirve el sitio, el panel y la API (ADR-022, ADR-023). La usan Docker Compose y Render; la base de producción está en Neon (ADR-021).
- `docker-compose.yml`: `db` (Postgres), `app` (la imagen única), `pgadmin` opcional (perfil `tools`).

## Convenciones del backend

- Paquetes por capa bajo `com.ventanasyaluminios`: `config`, `controller`, `dto/request`, `dto/response`, `entity`, `entity/enums`, `exception`, `mapper`, `repository`, `security`, `service` (interfaces) y `service/impl`. Inyección por constructor. Javadoc en español en cada clase.
- Los controllers solo reciben y devuelven DTOs (`record` de Java) y validan con `@Valid`; nunca exponen entidades.
- Mappers entidad ↔ DTO escritos a mano, una clase por módulo. Sin Lombok.
- Los errores pasan por un `@RestControllerAdvice` global que responde `{ "error": "<mensaje en español>", "code": "<CODIGO>" }`.
- API bajo `/api/v1`; administración bajo `/api/v1/admin/**` con rol `ADMIN`.
- El esquema solo cambia con migraciones nuevas de Flyway en `backend/src/main/resources/db/migration/` (`V<n>__descripcion.sql`). Una migración ejecutada nunca se edita. Hibernate corre con `ddl-auto=validate`. Los datos iniciales también son migraciones.
- Configuración tipada con `@ConfigurationProperties`, validada al arrancar. Al correr localmente se lee el `.env` de la raíz (ver `application.yml`).

## Convenciones del frontend

- Estilo visual: tokens de `apple-style-reference` (en `src/index.css`) y movimiento de `apple-design`. No se conserva nada del diseño del sitio viejo.
- antislop activo en modo **durante** para este proyecto (elegido por la usuaria): aplicar sus reglas al construir y entregar cada vista con su reporte del Delivery Gate.
- Estructura: `auth`, `components/{layout,ui}`, `lib`, `pages`, `routes`, `services/api`, `test`, `types`. Las páginas nunca llaman a axios directo: siempre a `src/services/api/*`; los tipos de `src/types/` reflejan los DTO del backend.
- Las afirmaciones de `docs/01-briefing.md` §6.3 están confirmadas como reales por la empresa.
- Las reglas del configurador de vidrio (briefing §3.5) se implementan en backend y frontend y se prueban con los mismos casos de ejemplo.

## Dónde está cada cosa

- `docs/01-briefing.md`: requisitos, especificación funcional y textos del sitio.
- `docs/02-modelo-datos.md`: tablas y relaciones; se actualiza con cada cambio de esquema (y `docs/diagramas/`).
- `docs/03-contrato-api.md`: rutas, DTO y códigos de error; el frontend trabaja contra este contrato.
- `docs/04-decisiones-tecnicas.md`: ADR; agregar uno por cada decisión no trivial.
- `docs/05-plan-de-trabajo.md`: pasos, ramas, commits, checklist de PR y pruebas.

## Comandos

| Tarea | Comando |
|---|---|
| Todo el sistema | `docker compose up -d --build` (sitio en http://localhost:8080) |
| Solo la base | `docker compose up -d db` |
| Backend: correr / probar | `cd backend` y luego `./mvnw spring-boot:run` / `./mvnw verify` (requiere Docker) |
| Frontend: dev / lint / pruebas / build | `npm run dev` / `npm run lint` / `npm test` / `npm run build` (desde la raíz o `frontend/`) |

## Referencia

El sitio viejo (`10Vero10/ventanasyaluminios`) es solo referencia de reglas de negocio y textos, nunca de estructura. Archivos útiles: `src/utils/blueprint.ts` (+ pruebas), `server/quotes.ts` (+ pruebas), `src/data/initialData.ts`.
