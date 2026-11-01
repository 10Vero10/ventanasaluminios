# Ventanas y Aluminios SAS

Sitio web y panel de administración de Ventanas y Aluminios SAS: catálogo de productos, galería de trabajos, configurador de vidrio a escala y gestión de cotizaciones.

| Carpeta | Contenido |
|---|---|
| `backend/` | API REST en Java 21 + Spring Boot 3.5, organizada por capas (controller, service, repository, entity, dto, mapper…), PostgreSQL y Flyway. Ver [backend/README.md](backend/README.md). |
| `frontend/` | React + Vite + TypeScript, Tailwind CSS, React Router y shadcn/ui. Se sirve con nginx. Ver [frontend/README.md](frontend/README.md). |
| `.github/` | CI en GitHub Actions: pruebas del backend, lint y build del frontend, y construcción de las imágenes Docker. |
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

Requisitos: Java 21, Node 22 y Docker (para PostgreSQL y las pruebas con Testcontainers). Funciona igual en Windows, macOS y Linux; también se puede abrir la carpeta en IntelliJ.

1. Crea el `.env` (si no lo hiciste) y levanta solo la base de datos:

   ```bash
   docker compose up -d db
   ```

2. Backend en http://localhost:8080. Lee el `.env` de la raíz automáticamente, no hay que exportar variables:

   ```bash
   cd backend
   ```

   ```bash
   ./mvnw spring-boot:run
   ```

   En Windows (PowerShell o CMD) usa `.\mvnw.cmd spring-boot:run`. En IntelliJ basta con ejecutar `VentanasYAluminiosApplication`.

3. Frontend en http://localhost:5173, desde la raíz del repo. Vite reenvía `/api` y `/media` al backend local:

   ```bash
   npm install
   ```

   ```bash
   npm run dev
   ```

   `npm install` en la raíz también instala las dependencias de `frontend/`. Los scripts `dev`, `build`, `lint` y `preview` de la raíz ejecutan los del frontend.

## Pruebas

Backend (necesita Docker en ejecución para Testcontainers):

```bash
cd backend
```

```bash
./mvnw verify
```

En Windows: `.\mvnw.cmd verify`.

Frontend:

```bash
npm run build && npm run lint
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
