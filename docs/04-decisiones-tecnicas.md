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
- **Opción por defecto en la base:** `is_default` en cada tabla (Crudo, Transparente, 6 mm), para que el configurador no tenga esos valores fijos en el código. Debe estar activa. "Como máximo una por tabla" es una restricción `EXCLUDE` diferible: al marcar otra, la anterior se desmarca en la misma transacción sin importar el orden de escritura; dos cambios simultáneos chocan al confirmar y responden 409.
- **Duplicar:** la copia se llama "… (copia)" (recortada a 80 caracteres), queda al final y **oculta**, para que no aparezca en el sitio antes de editarla.
- **Productos ocultos:** el sitio público no los lista y su detalle responde 404; el panel los ve todos.
- **Las tres opciones de vidrio comparten `GlassOption`** (`@MappedSuperclass`: orden, activa, por defecto) y `GlassOptionRepository`; todas las listas ordenables usan `PositionedRepository` y `Reordering.apply`. El código (o los mm) se recibe en el constructor y no tiene setter.
- **Ediciones cruzadas de un producto:** `product.version` (`@Version`). Sin él, dos ediciones simultáneas de la ficha técnica podían dejar huecos en las posiciones y el catálogo entero respondía 500. La segunda edición recibe 409.
- **Carácter nulo:** un `@JsonComponent` rechaza U+0000 en cualquier texto del JSON (400), porque PostgreSQL no lo admite.
- **Carga en lote:** `hibernate.default_batch_fetch_size = 50`, para que listar el catálogo no haga una consulta por producto.

## ADR-014 — Módulo quote

- **Orden de validación:** el campo trampa se revisa antes que la validación, para responder éxito a un bot sin darle pistas. Por eso `POST /quotes` valida en el service con `Validator` y no con `@Valid` en el controller.
- **El servidor recalcula el configurador:** área y texto de medidas se calculan en `GlassConfiguratorRules` con los nombres actuales de las opciones; el texto que manda el navegador se descarta. Las opciones inactivas se rechazan. Los mismos casos de prueba se usan en el frontend.
- **Límite por IP:** filtro propio (`RateLimitFilter` + `RateLimiter`) con contador en memoria por ventanas fijas, configurable en `app.rate-limit`. Cuenta todo intento a la ruta (también los inválidos y los del campo trampa). IPv6 se agrupa por /64 y la memoria se acota a 100 000 clientes; si se llena, los clientes nuevos reciben 429 hasta que venzan ventanas. Vale para una sola instancia del backend.
- **IP real del cliente:** `server.forward-headers-strategy=native`. Tomcat acepta `X-Forwarded-For` solo de proxies internos e ignora `Forwarded`; nginx reemplaza `X-Forwarded-For` y borra `Forwarded` y `X-Forwarded-Host/Port/Prefix`. Con `framework`, un cliente podía elegir su IP con el encabezado `Forwarded` y anular el límite (hallazgo de la revisión de seguridad, cubierto por `ForwardedHeaderIntegrationTest`).
- **Borrado lógico:** `@SQLRestriction("deleted_at IS NULL")` oculta las eliminadas de toda consulta; el service además trata como inexistente una borrada en la misma transacción.
- **Historial:** tabla `quote_status_change` (nombre final de `quote_status_history`). Repetir el mismo estado no crea entrada. `changed_by` queda nulo hasta el paso 4.
- **Bandeja:** orden por `createdAt` (y `id` para desempatar), ascendente por defecto; `size` máximo 100. El cuerpo de `POST /quotes` se limita a 64 KB en nginx.

## ADR-015 — Autenticación del panel

- **JWT en cookie:** HS256 con `JWT_SECRET` (obligatoria, 32+ caracteres; sin ella el backend no arranca). Se firma y verifica con `spring-security-oauth2-jose` (Nimbus). Algoritmo fijo, emisor y vencimiento verificados. Cookie `vya_session` httpOnly, `SameSite=Lax`, `Path=/api`, `Secure` por defecto.
- **Sin oauth2-resource-server:** desactiva CSRF en toda solicitud con token; con el token en una cookie que el navegador adjunta solo, eso abría el panel a CSRF (lo detectó `AuthIntegrationTest`). Un filtro propio (`SessionCookieAuthenticationFilter`) lee la cookie solo en rutas con sesión, así una cookie vencida no rompe las páginas públicas.
- **Revocación:** cada solicitud verifica contra la base que el administrador esté activo y que el `ver` del token sea su `token_version`. Cambiar la contraseña o cerrar sesión sube la versión e invalida todas las sesiones anteriores, aunque alguien haya copiado el token.
- **CSRF:** `CookieCsrfTokenRepository` (cookie `XSRF-TOKEN` legible por JavaScript, encabezado `X-XSRF-TOKEN`) en todo cambio con sesión. Exentos solo `POST /quotes` y `POST /auth/login`, que no tienen sesión que proteger.
- **Contraseña temporal:** el administrador inicial (ADR-005) entra con rol temporal y solo puede cambiar la contraseña (403 `PASSWORD_CHANGE_REQUIRED` en el panel). BCrypt costo 12; contraseñas de 12 a 72 bytes (BCrypt ignora lo que pasa de 72 y se rechaza en vez de recortarse).
- **Login:** el mismo mensaje y el mismo costo (BCrypt contra un hash de referencia) para correo inexistente, inactivo o contraseña equivocada. 5 intentos por minuto por IP con el mismo filtro del ADR-014.
- **Respaldo:** usa los DTO de entrada de cada módulo y sus services, así cumple las mismas reglas que el panel. Importar reemplaza líneas destacadas, galería y productos en una sola transacción; las opciones de vidrio se actualizan por código y las ausentes se desactivan (no se borran). Las cotizaciones no forman parte del respaldo.

## ADR-016 — Frontend público

- **Configuración del sitio una sola vez:** `SiteProvider` carga `GET /site` al entrar y la reparte con `useSite()`. Sin ella no se pinta el sitio (carga o error con reintento), así ningún texto aparece con valores fijos que luego cambian. También pone el título de la pestaña, la meta descripción y el favicon.
- **Estados de datos:** `useLoad` da carga, error con mensaje en español y datos, con reintento; toda vista con datos muestra los tres (antislop R-27).
- **Filtros en la URL:** `?linea=ventanas` en catálogo y trabajos, para compartir el enlace; un valor desconocido equivale a "todas".
- **Configurador:** el estado vive en la página de cotización y se sincroniza con el formulario en los manejadores de eventos (no en efectos). Las hojas siguen la sugerencia por ancho hasta que el visitante elige otra. Se envían los datos estructurados de la última configuración válida aplicada; el servidor recalcula el texto (ADR-014).
- **Movimiento:** `motion` con springs sin rebote (apple-design) en el diálogo, el menú y el plano; con `prefers-reduced-motion` se usa un fundido corto o ningún movimiento.
- **Plano:** SVG a escala real; el lienzo mide 600 × 260 cm a la escala que cabe en el ancho disponible, la misma en ancho y alto.

## ADR-017 — Frontend del panel

- **Sesión sin token en JavaScript:** el panel nunca ve el JWT (cookie httpOnly). `AuthProvider` pregunta `/auth/me` al abrir; un 401 en cualquier ruta del panel lo avisa `httpClient` y la vista vuelve al login. `RequireAdmin` solo evita pintar pantallas que el backend rechazaría.
- **CSRF:** axios envía la cookie `XSRF-TOKEN` en `X-XSRF-TOKEN` en las solicitudes al mismo dominio, sin código extra.
- **Configuración por pestañas:** Inicio, Quiénes somos y Configuración editan partes de la misma configuración; cada una guarda la configuración completa leída al abrirla (`PUT /admin/settings` reemplaza todo).
- **Orden de listas:** subir y bajar cambian la pantalla al instante y guardan el orden completo; si el servidor lo rechaza, vuelve el orden anterior con el error.
- **Carga aparte:** el panel es un `lazy()` en `/admin/*`, así el sitio público no descarga su código.
- **Textos de cotizaciones:** se muestran como texto (React escapa todo), nunca como HTML.

## ADR-018 — Borradores y listas del panel

- **Borrador que sigue al servidor (`useDraft`):** cada formulario del panel edita una copia. Si llega una versión nueva del objeto (recarga, otra fila guardada), los campos sin tocar toman el valor del servidor y los editados se conservan. Así una recarga no borra lo escrito y, por ejemplo, la opción de vidrio que dejó de ser "por defecto" se ve desmarcada.
- **Reordenar (`useReorder`):** el cambio se ve al instante y se guarda el orden completo; si falla, vuelve el orden anterior solo si no llegaron datos nuevos mientras tanto. Mientras la lista se recarga, subir y bajar quedan deshabilitados.
- **Cotizar:** el armado de la solicitud y la sincronización con el configurador son funciones puras (`quoteDraft.ts`) y la página las aplica con actualizaciones funcionales, para que el fin de un envío no pise lo escrito mientras tanto.
- **Respaldo:** las conversiones respuesta → solicitud viven en los mappers. Un respaldo que no trae un campo agregado después (como `aboutImageUrl`) conserva el valor actual en vez de borrarlo; una prueba verifica que la exportación incluya todos los campos de la configuración.

## ADR-019 — Publicación en Render y Vercel

- **Reparto:** backend (Docker) y PostgreSQL en Render con `render.yaml`; frontend estático en Vercel con `frontend/vercel.json`. Vercel reescribe `/api/*` y `/media/*` hacia el backend y todo lo demás a `index.html`, así el navegador ve un solo dominio: la cookie de sesión (`SameSite=Lax`, `Path=/api`) y el CSRF funcionan sin CORS.
- **Base de datos:** Render entrega host, puerto, base, usuario y clave por separado; `application.yml` arma la URL JDBC con `DB_HOST` cuando no hay `DB_URL`. `JWT_SECRET` lo genera Render; el administrador inicial se pide al crear el Blueprint.
- **Límites conocidos:**
  - Plan free de Render: el servicio se duerme tras 15 minutos sin tráfico y despertar tarda cerca de un minuto (Java + migraciones). La base free vence según las condiciones vigentes de Render; para producción, plan starter y base de pago.
  - Imágenes subidas: el disco del contenedor se borra en cada despliegue. Para conservarlas hace falta un disco persistente de Render (de pago) montado en `/app/uploads`, o usar URLs de imágenes alojadas.
  - Vercel limita el cuerpo de las solicitudes reenviadas (unos 4,5 MB): una imagen cercana a 5 MB puede fallar al subirse por esta vía.
  - Límite por IP: detrás de Vercel y del proxy de Render, Tomcat solo confía en proxies internos, así que la IP que ve es la del borde de Vercel y varios visitantes pueden compartir un mismo contador. Es lo seguro (no se puede falsear la IP llamando directo a Render), a cambio de un límite más grueso.
