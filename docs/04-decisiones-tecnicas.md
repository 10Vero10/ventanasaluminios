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

## ADR-012 — Detalles del módulo settings

- **Imágenes de la galería sin línea:** `gallery_image.category` es opcional. "Taller de Carpintería y Ensamble" no pertenece a ninguna línea y solo aparece en el filtro "Todos".
- **Validación de imágenes subidas:** por la firma de bytes del archivo (JPG, PNG, WebP), no por la extensión ni el `Content-Type`. Nombre aleatorio (UUID). Máximo 5 MB. SVG no se acepta porque puede ejecutar código.
- **Favicon por defecto:** el logo de 4 paneles de la empresa, servido como `/favicon.svg` desde el frontend.
- **Reordenar:** se envía la lista completa de ids; si no coincide con los existentes, se rechaza (`REORDER_MISMATCH`) para no perder elementos agregados en otra pestaña. Toda entidad ordenable implementa `Positioned` y usa `Reordering.apply` (también las del catálogo).
- **WhatsApp:** se guarda solo con dígitos; si viene vacío se toma del teléfono. El resultado debe tener entre 7 y 20 dígitos y se valida antes de modificar la configuración.
- **Creaciones simultáneas:** dos líneas o imágenes creadas a la vez pueden pedir la misma posición. La restricción única lo detecta al confirmar y el manejador responde 409 (`CONFLICT`) pidiendo intentar de nuevo. Con un solo administrador no se justifica bloquear la tabla.
- **Archivos de imágenes borradas:** eliminar una imagen de la galería no borra el archivo de `/media`, porque puede seguir en uso como portada o en una línea destacada.
- **Errores:** `GlobalExceptionHandler` hereda de `ResponseEntityExceptionHandler` para que los errores de Spring MVC conserven su estado HTTP; solo los 5xx se registran como ERROR.
- **Panel abierto hasta el paso 4:** las rutas `/api/v1/admin/**` existen desde el paso 1 pero se protegen en el paso 4. No se despliega antes.

## ADR-013 — Módulo catalog

- **Ficha técnica y características como listas del producto:** `@ElementCollection` con `@OrderColumn`, sin id propio. El panel envía la lista completa en el orden deseado al guardar; no hacen falta endpoints para agregar, quitar o reordenar filas. Reemplaza `product_spec.id` y `product_feature.id` de la propuesta inicial.
- **Opciones de vidrio no se borran:** se desactivan (`active: false`), y su código (o los mm) no cambia después de creada (`IMMUTABLE_FIELD`), porque las cotizaciones guardan ese valor. Para corregir un código se crea una opción nueva y se desactiva la anterior.
- **Opción por defecto en la base:** `is_default` en cada tabla (Crudo, Transparente, 6 mm) con un índice único parcial, para que el configurador no tenga esos valores fijos en el código. Debe estar activa. Al marcar otra, la anterior se desmarca y se escribe primero.
- **Duplicar:** la copia se llama "… (copia)" (recortada a 80 caracteres), queda al final y **oculta**, para que no aparezca en el sitio antes de editarla.
- **Productos ocultos:** el sitio público no los lista y su detalle responde 404; el panel los ve todos.
- **Las tres opciones de vidrio comparten `GlassOption`** (`@MappedSuperclass`: orden, activa, por defecto), y todas las listas ordenables usan `Reordering.apply`.
