# Ventanas y Aluminios SAS

Sitio web y panel de administración de Ventanas y Aluminios SAS: catálogo de productos, galería de trabajos, configurador de vidrio a escala y gestión de cotizaciones.

| Carpeta | Contenido |
|---|---|
| `backend/` | API REST en Java 21 + Spring Boot 3.5, organizada por capas (controller, service, repository, entity, dto, mapper…), PostgreSQL y Flyway. Ver [backend/README.md](backend/README.md). |
| `frontend/` | React + Vite + TypeScript, Tailwind CSS, React Router y shadcn/ui. Se sirve con nginx. Ver [frontend/README.md](frontend/README.md). |
| `.github/` | CI en GitHub Actions: pruebas del backend, lint y build del frontend, y construcción de las imágenes Docker. |
| `docs/` | [01 Briefing](docs/01-briefing.md), [02 Modelo de datos](docs/02-modelo-datos.md), [03 Contrato de la API](docs/03-contrato-api.md), [04 Decisiones (ADR)](docs/04-decisiones-tecnicas.md), [05 Plan de trabajo y ramas](docs/05-plan-de-trabajo.md) y `diagramas/`. |
| `docker-compose.yml` | PostgreSQL 16, backend, nginx y pgAdmin (opcional). |

## Levantar todo con Docker

Requisitos: Docker con Docker Compose.

```bash
cp .env.example .env
```

Edita `.env` y cambia las claves. `JWT_SECRET` es obligatoria (mínimo 32 caracteres) y `ADMIN_EMAIL` / `ADMIN_INITIAL_PASSWORD` crean el primer administrador, que debe cambiar la contraseña en su primer ingreso. Si ya tenías un `.env` de una versión anterior, agrégale esas variables desde `.env.example`. Luego:

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

## Publicar en internet (Render + Vercel)

El backend y PostgreSQL van en Render (`render.yaml`) y el frontend en Vercel (`frontend/vercel.json`). Vercel reenvía `/api` y `/media` al backend, así el sitio sigue en un solo dominio y la sesión del panel funciona igual.

1. **Render** → New → Blueprint → elige este repositorio. Crea `ventanasyaluminios-api` (Docker, desde `main`) y la base `ventanasyaluminios-db`. Pide `ADMIN_EMAIL` y `ADMIN_INITIAL_PASSWORD` (mínimo 12 caracteres); `JWT_SECRET` lo genera Render.
2. Cuando el servicio esté en verde, comprueba `https://ventanasyaluminios-api.onrender.com/api/v1/health` (debe responder `{"status":"ok"}`). Si Render le dio otra dirección, cámbiala en `frontend/vercel.json`.
3. **Vercel** → proyecto con Root Directory `frontend`, preset Vite, sin variables de entorno. En Settings → Git, Production Branch `main`.
4. Entra a `https://<tu-proyecto>.vercel.app/admin` con el correo y la contraseña temporal y crea la tuya.

Límites de los planes gratis (detalle en ADR-019): el backend se duerme tras 15 minutos sin visitas y tarda cerca de un minuto en despertar; la base gratis de Render vence; las imágenes subidas desde el panel se pierden en cada despliegue sin un disco persistente (de pago); Vercel limita el cuerpo de cada solicitud a unos 4,5 MB.

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
npm run lint && npm test && npm run build
```

## Ramas

`main` guarda lo entregado y estable; `develop` es la rama de integración. Cada paso se trabaja en una rama `feature/*` que sale de `develop` y vuelve por Pull Request con el CI en verde. Commits con [Conventional Commits](https://www.conventionalcommits.org/es/). Detalle en [docs/05-plan-de-trabajo.md](docs/05-plan-de-trabajo.md).

## Estado

| Paso | Alcance | Estado |
|---|---|---|
| 0 | Estructura, Docker Compose, proyectos inicializados | Hecho (verificado con `docker compose up`) |
| 1 | Base del backend y módulo `settings` | Hecho |
| 2 | Módulo `catalog` | Hecho |
| 3 | Módulo `quote` | Hecho |
| 4 | Módulo `auth` | Hecho |
| 5 | Frontend conectado a la API | Hecho |
| 6 | Despliegue | Hecho: Docker Compose en el equipo de la empresa (sin migración desde Redis: se empezó con datos nuevos) |
