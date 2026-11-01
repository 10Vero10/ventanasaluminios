# Ventanas y Aluminios SAS

Sitio web y panel de administración de Ventanas y Aluminios SAS: catálogo de productos, galería de trabajos, configurador de vidrio a escala y gestión de cotizaciones.

| Carpeta | Contenido |
|---|---|
| `backend/` | API REST en Java 21 + Spring Boot 3.5, arquitectura en capas (controller, service, repository, entity), PostgreSQL y Flyway. Ver [backend/README.md](backend/README.md). |
| `frontend/` | React + Vite + TypeScript, Tailwind CSS y React Router. Se sirve con nginx. |
| `docs/` | [Briefing](docs/briefing.md) original y [decisiones](docs/decisiones.md) tomadas durante la reconstrucción. |
| `docker-compose.yml` | PostgreSQL 16, backend, nginx y pgAdmin (opcional). |

## Levantar todo con Docker

Requisitos: Docker con Docker Compose.

```bash
cp .env.example .env
```

Edita `.env` y cambia las claves. Luego:

```bash
docker compose up -d --build
```

El sitio queda en http://localhost:8080 (o el puerto de `WEB_PORT`). nginx sirve el frontend y reenvía `/api` y `/media` al backend, así todo queda en un solo dominio.

Comprobar que la API responde:

```bash
curl http://localhost:8080/api/v1/health
```

pgAdmin es opcional y se levanta con su perfil. Queda en http://localhost:5050:

```bash
docker compose --profile tools up -d
```

Detener todo (los datos se conservan en los volúmenes `pgdata` y `uploads`):

```bash
docker compose down
```

## Desarrollo local

Requisitos: Java 21, Node 22 y Docker (para PostgreSQL y las pruebas con Testcontainers).

Solo la base de datos en Docker:

```bash
docker compose up -d db
```

Backend en http://localhost:8080, leyendo las variables del `.env`:

```bash
cd backend && set -a && . ../.env && set +a && DB_USER=$POSTGRES_USER DB_PASSWORD=$POSTGRES_PASSWORD DB_URL=jdbc:postgresql://localhost:${POSTGRES_PORT:-5432}/$POSTGRES_DB ./mvnw spring-boot:run
```

Frontend en http://localhost:5173 (Vite reenvía `/api` y `/media` al backend local):

```bash
cd frontend && npm install && npm run dev
```

## Pruebas

Backend (necesita Docker en ejecución para Testcontainers):

```bash
cd backend && ./mvnw verify
```

Frontend:

```bash
cd frontend && npm run build && npm run lint
```

## Estado

| Paso | Alcance | Estado |
|---|---|---|
| 0 | Estructura, Docker Compose, proyectos inicializados | Hecho |
| 1 | Base del backend y módulo `settings` | Pendiente |
| 2 | Módulo `catalog` | Pendiente |
| 3 | Módulo `quote` | Pendiente |
| 4 | Módulo `auth` | Pendiente |
| 5 | Frontend conectado a la API | Pendiente |
| 6 | Migración desde Redis y despliegue | Pendiente |
