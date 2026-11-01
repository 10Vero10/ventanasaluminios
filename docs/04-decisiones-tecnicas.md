# 04 — Decisiones técnicas y de producto (ADR)

Registro de decisiones. Cuando hay diferencia con `01-briefing.md`, manda este archivo. Las decisiones registradas aquí no se vuelven a preguntar; una decisión nueva no trivial se agrega como ADR al final.

Formato: contexto → decisión → consecuencias.

## ADR-001 — Stack

- **Contexto:** el sitio viejo no tenía backend real (localStorage, Redis y PIN fijo).
- **Decisión:** backend Java 21 + Spring Boot 3.5 (Web, Data JPA, Security + JWT, Validation), PostgreSQL 16, Flyway y Maven; frontend React + Vite + TypeScript strict, Tailwind v4, React Router, axios y shadcn/ui. Pruebas con JUnit 5, Mockito, Testcontainers y Vitest.
- **Consecuencias:** no se agregan librerías al backend sin consultar. TypeScript queda en 6.0 hasta que typescript-eslint soporte la 7.

## ADR-002 — Estructura del repositorio y del código

- **Contexto:** la usuaria dio como referencia el proyecto SIERAP.
- **Decisión:** raíz con `backend/`, `frontend/`, `docs/` (numerados), `.github/`, `.gitattributes`, `CLAUDE.md`, `docker-compose.yml`. Backend **por capas** bajo `com.ventanasyaluminios`: `config`, `controller`, `dto/request`, `dto/response`, `entity`, `entity/enums`, `exception`, `mapper`, `repository`, `security`, `service` (interfaces) y `service/impl`. Frontend con `auth`, `components/{layout,ui}`, `lib`, `pages`, `routes`, `services/api`, `test`, `types`.
- **Consecuencias:** reemplaza la organización por módulo del briefing §2.4. Las páginas del frontend nunca llaman a axios directo.

## ADR-003 — Flujo de ramas

- **Contexto:** se sigue la estrategia de SIERAP.
- **Decisión:** `main` (entregado y estable), `develop` (integración) y ramas temporales desde `develop`. Detalle en `05-plan-de-trabajo.md`.
- **Consecuencias:** cada paso se trabaja en una rama `feature/*` y entra a `develop` por PR con el CI en verde.

## ADR-004 — Despliegue en un solo dominio

- **Contexto:** el JWT viaja en cookie; con frontend y backend en dominios distintos sería una cookie de terceros que Safari bloquea.
- **Decisión:** `docker compose` levanta nginx (sirve el frontend y reenvía `/api` y `/media`), backend y PostgreSQL. El servidor de destino se define en el paso 6.
- **Consecuencias:** nginx reemplaza `X-Forwarded-For` con la IP real para que el límite por IP no se pueda falsear.

## ADR-005 — Primer administrador

- **Decisión:** se crea al arrancar desde variables de entorno si no existe ninguno, con cambio de contraseña obligatorio en el primer ingreso.
- **Consecuencias:** la contraseña nunca queda en el repositorio ni en una migración.

## ADR-006 — Estados de cotización libres con historial

- **Decisión:** el administrador puede cambiar el estado libremente (incluso hacia atrás). Cada cambio queda en `quote_status_history`.
- **Consecuencias:** se tolera el error humano sin perder el rastro.

## ADR-007 — Configurador: hojas a elección del cliente

- **Decisión:** el cliente elige 1 a 4 hojas; el configurador propone `max(1, ceil(ancho / 150))`. Se guarda en `quote.panes` y aparece en el texto de medidas cuando es mayor que 1: `"… · Vidrio crudo transparente 6 mm · 3 hojas"`. El color se llama "Verde Frence".

## ADR-008 — Galería de trabajos pública

- **Decisión:** página "Galería de trabajos" en `/trabajos`; cada imagen tiene título y línea, y se filtra por línea. En el catálogo se quita la etiqueta "GALERÍA DE TRABAJOS" de cada tarjeta y se agrega el enlace "Ver trabajos de esta línea". Botones: "Cotizar portón americano" y "Cotizar ventana corrediza".

## ADR-009 — Estilo visual y revisión de interfaz

- **Decisión:** estilo nuevo con `apple-style-reference` (paleta, tipografía, radios, superficies) y `apple-design` (animación y botones). No se conserva nada del diseño viejo. `antislop` en modo **durante**.
- **Consecuencias:** cada vista se entrega con el reporte del Delivery Gate de antislop.

## ADR-010 — Contenido y respaldo

- **Decisión:** las afirmaciones del briefing §6.3 están confirmadas como reales. Las imágenes del sitio viejo eran de prueba: se usan como provisionales. El respaldo usa un formato nuevo (versión 1), sin importar respaldos del sitio viejo.

## ADR-011 — Revisión de seguridad y calidad

- **Decisión:** `security-audit` al cerrar los pasos 3 y 4 y antes del despliegue; `thermos` (lo invoca la usuaria) al cerrar cada paso.
