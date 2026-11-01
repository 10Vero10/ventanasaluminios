# 05 — Plan de trabajo, ramas y convenciones

## 1. Pasos

| Paso | Alcance | Rama |
|---|---|---|
| 0 | Estructura del repo, Docker Compose, proyectos inicializados, CI | `develop` (arranque) |
| 1 | Base del backend (errores, configuración, CORS) y módulo `settings` (configuración, líneas destacadas, galería) con migraciones y datos iniciales | `feature/settings` |
| 2 | Módulo `catalog` (productos, specs, características, opciones de vidrio) con los 8 productos | `feature/catalog` |
| 3 | Módulo `quote`: creación pública con validaciones y límite por IP; bandeja del administrador | `feature/quotes` |
| 4 | Módulo `auth`: login, JWT en cookie, protección de rutas, administrador inicial; respaldo | `feature/auth` |
| 5 | Frontend conectado a la API: sitio público, configurador y panel | `feature/frontend-*` (una por vista grande) |
| 6 | Migración de cotizaciones desde Upstash Redis y despliegue | `feature/redis-migration`, `release/v1.0.0` |

Un paso está terminado cuando compila, las pruebas pasan, el CI está en verde y `docker compose up` funciona.

## 2. Estrategia de ramas

### 2.1 Ramas permanentes

| Rama | Propósito | Reglas |
|---|---|---|
| `main` | **Producción.** Solo código entregado y estable. | Nadie hace push directo. Solo recibe merges de `release/*` y `hotfix/*`. Cada merge lleva una etiqueta (`v1.0.0`). |
| `develop` | **Integración.** Estado actual del desarrollo; siempre compila y pasa el CI. | Solo recibe merges por Pull Request aprobado. |

### 2.2 Ramas temporales

Salen **de `develop`** y vuelven **a `develop`** (salvo `hotfix/*`). Se borran después del merge.

| Prefijo | Para qué | Ejemplo |
|---|---|---|
| `feature/` | Funcionalidad nueva | `feature/settings` |
| `fix/` | Corrección de un error en desarrollo | `fix/quote-rate-limit` |
| `test/` | Pruebas agregadas aparte | `test/configurator-cases` |
| `refactor/` | Reorganización sin cambio de comportamiento | `refactor/product-mapper` |
| `docs/` | Solo documentación o diagramas | `docs/class-diagram` |
| `chore/` | Configuración, dependencias, CI | `chore/ci-docker` |
| `release/` | Congelar antes de una entrega | `release/v1.0.0` |
| `hotfix/` | Corrección urgente de algo ya entregado | `hotfix/login-roto` |

Nombres: `<prefijo>/<descripcion-en-kebab-case>`.

### 2.3 Release y hotfix

- `release/*` sale de `develop` cuando se congela una entrega; solo recibe correcciones y al terminar se fusiona en **`main` y de vuelta en `develop`**.
- `hotfix/*` sale de **`main`** y se fusiona en **`main` y `develop`**. Es la única rama que se salta `develop`.

### 2.4 Flujo diario

```bash
git checkout develop
```

```bash
git pull origin develop
```

```bash
git checkout -b feature/settings
```

Commits pequeños y frecuentes, push y PR hacia `develop`:

```bash
git push -u origin feature/settings
```

Si la rama dura varios días, traer `develop` seguido:

```bash
git merge origin/develop
```

### 2.5 Convención de commits

[Conventional Commits](https://www.conventionalcommits.org/es/), con la descripción en español:

```
feat(settings): configuración del sitio y líneas destacadas
fix(quote): rechazar cotizaciones con el interruptor apagado
test(configurator): casos de área y texto de medidas
docs(api): contrato de la galería
chore(ci): construir imágenes Docker
refactor(catalog): extraer ProductMapper
```

Alcances sugeridos: `settings`, `catalog`, `gallery`, `glass`, `quote`, `auth`, `backup`, `media`, `ui`, `ci`, `docker`, `docs`.

### 2.6 Checklist de Pull Request

- [ ] Compila y el CI está en verde.
- [ ] Tiene pruebas de lo que agrega (Mockito para services, Testcontainers para repositories e integración, Vitest en el frontend).
- [ ] Respeta las capas: sin lógica de negocio en el controller, ninguna entidad cruza la API (siempre DTOs).
- [ ] Nombres estándar de Java: paquetes en minúscula, clases en `PascalCase`, métodos y atributos en `camelCase`.
- [ ] Clases nuevas con Javadoc en español.
- [ ] Sin `try/catch` dispersos: las excepciones se manejan en el `@RestControllerAdvice`.
- [ ] Mensajes para el usuario en español.
- [ ] Ninguna migración ya presente en `develop` fue editada.
- [ ] Si cambió el modelo: `02-modelo-datos.md` actualizado. Si cambió una ruta: `03-contrato-api.md` actualizado.
- [ ] Si fue una decisión no trivial: nuevo ADR en `04-decisiones-tecnicas.md`.
- [ ] Si toca interfaz: reporte del Delivery Gate de antislop.

### 2.7 Protecciones en GitHub

En `Settings → Branches`, para `main` y `develop`:

- Exigir Pull Request antes de fusionar.
- Exigir que el CI pase.
- Exigir que la rama esté al día antes de fusionar.
- Bloquear force-push y borrado.

## 3. Pruebas

| Capa | Herramienta |
|---|---|
| Services | JUnit 5 + Mockito |
| Repositories e integración | Testcontainers con PostgreSQL 16 |
| Controllers | `@WebMvcTest` |
| Frontend | Vitest + Testing Library |
| Reglas del configurador | Los mismos casos de ejemplo en backend y frontend |
