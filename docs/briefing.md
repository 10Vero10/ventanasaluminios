# Briefing — Reconstrucción de Ventanas y Aluminios SAS

> **Para Claude (chat nuevo):** este documento reúne todo lo necesario para reconstruir el sitio desde cero.
> Contiene las instrucciones de trabajo, el stack definido, la especificación funcional, el contenido textual completo, un modelo de datos y una lista de endpoints como punto de partida, y las decisiones pendientes.
> Léelo completo antes de empezar. Ante cualquier duda o ambigüedad, **pregunta antes de asumir**.

---

## Índice

1. [Repositorios](#1-repositorios)
2. [Instrucciones de trabajo](#2-instrucciones-de-trabajo)
3. [Especificación funcional](#3-especificación-funcional)
4. [Modelo de datos y endpoints (punto de partida)](#4-modelo-de-datos-y-endpoints-punto-de-partida)
5. [Contenido textual del sitio](#5-contenido-textual-del-sitio)
6. [Observaciones a corregir y decisiones pendientes](#6-observaciones-a-corregir-y-decisiones-pendientes)

---

## 1. Repositorios

| Repositorio | Enlace | Uso |
|---|---|---|
| **Nuevo** (donde se trabaja) | https://github.com/10Vero10/ventanasaluminios | Vacío: solo tiene `README.md` y `.gitignore`. Aquí va la versión nueva. |
| **Viejo** (solo referencia) | https://github.com/10Vero10/ventanasyaluminios | Sitio actual en producción (React + Vite en Vercel). **No se modifica.** Sirve para consultar comportamiento, textos y reglas. |

### Archivos útiles del repo viejo (rama `main`)

| Archivo | Qué contiene |
|---|---|
| `src/data/initialData.ts` | Contenido por defecto del sitio, las 4 líneas destacadas, las 5 imágenes de galería y los **8 productos** (base para las semillas de Flyway). |
| `src/utils/blueprint.ts` (+ `blueprint.test.ts`) | Reglas del configurador de vidrio: rangos, área, tipos, colores, espesores, textos de medidas y mensaje sugerido, y combinación con el texto del cliente. |
| `server/quotes.ts` (+ `quotes.test.ts`) | Validaciones de la cotización: campos obligatorios, largos máximos, límite por IP, campo trampa, tope global y mensajes de error. |
| `src/components/quote/WindowBlueprint.tsx` | Componente visual del plano. Se puede reutilizar en el frontend nuevo. |
| `src/components/views/*.tsx` | Vistas actuales (Inicio, Catálogo, Nosotros, Cotizar, Admin), como referencia visual. |
| `src/utils/backup.ts` | Formato del respaldo JSON (`{ version, exportedAt, content, products }`). |

> El código viejo está mal organizado: componentes enormes, `localStorage` como base de datos y tres formas distintas de guardar datos. **No copies su estructura.** Úsalo solo para entender reglas y textos. La fuente de verdad son las secciones 3 a 6 de este documento.

---

## 2. Instrucciones de trabajo

### 2.1 Contexto
**Ventanas y Aluminios SAS** es una empresa colombiana que fabrica e instala a medida ventanas, puertas, portones y divisiones en vidrio y aluminio, con más de 25 años de experiencia.

El sitio actual funciona, pero no tiene backend real:
- el contenido se guarda en el `localStorage` del navegador más un archivo JSON en el código;
- las cotizaciones se guardan en Upstash Redis mediante una función serverless de Vercel;
- el acceso al panel es un PIN fijo.

El objetivo es **rehacerlo con arquitectura en capas (controller → service → repository → entity) y PostgreSQL**.

### 2.2 Stack definido

**Backend** (no agregues otras librerías sin consultarme):

| Pieza | Uso |
|---|---|
| **Java 21** | Lenguaje |
| **Spring Boot 3.5** | Framework |
| **Spring Web** | API REST |
| **Spring Data JPA + Hibernate** | ORM: entidades y repositorios |
| **Spring Security + JWT** | Login con token y permisos por rol (`ADMIN`) |
| **Bean Validation** | Validación de los datos que llegan |
| **PostgreSQL 16** | Base de datos, en Docker |
| **Flyway** | Migraciones SQL versionadas |
| **Maven** | Construcción y dependencias |
| **JUnit 5 + Testcontainers** | Pruebas: Mockito para services, PostgreSQL real para repositories e integración |

**Frontend:**
- React + Vite + TypeScript (`strict`).
- React Router con rutas reales: `/`, `/catalogo`, `/nosotros`, `/cotizar`, `/admin/...`.
- Tailwind CSS, conservando el estilo visual del sitio actual.

**Consecuencias de este stack (decididas):**
- Los mappers entidad ↔ DTO se escriben **a mano**, una clase por módulo.
- El límite de envíos por IP es un **filtro propio de Spring** con contador en memoria.
- Las imágenes se guardan como **URL externa** o en una **carpeta del servidor montada como volumen** de Docker. Nunca como base64 en la base.
- Los endpoints se documentan en el README del backend.
- Los DTOs son `record` de Java. No usar Lombok en las entidades.

### 2.3 Estructura del repositorio nuevo
```
/backend             → Spring Boot (Maven)
/frontend            → React + Vite + TypeScript
/docker-compose.yml  → postgres (y pgAdmin opcional)
/.env.example        → variables; el .env real nunca se sube
/docs                → este documento
```

### 2.4 Arquitectura del backend
```
com.ventanasyaluminios
  ├─ settings/  catalog/  quote/  auth/
  │    ├─ controller/   → REST, solo DTOs, @Valid
  │    ├─ service/      → reglas de negocio y transacciones
  │    ├─ repository/   → interfaces Spring Data JPA
  │    ├─ entity/       → @Entity JPA
  │    ├─ dto/          → records (request/response)
  │    └─ mapper/       → conversión entidad ↔ DTO, a mano
  └─ common/            → manejo de errores, configuración, seguridad, filtro de límite por IP
```

**Reglas:**
- Los controllers **nunca** exponen entidades.
- Un `@RestControllerAdvice` global devuelve errores con el formato `{ "error": "...", "code": "..." }` y mensajes en español.
- API bajo `/api/v1`. Las rutas de administración van bajo `/api/v1/admin/**`, protegidas con rol `ADMIN`.
- Contraseñas con BCrypt. El JWT viaja en **cookie httpOnly**.
- Configuración tipada con `@ConfigurationProperties`, validada al arrancar.
- Las reglas del configurador de vidrio (sección 3.5) viven en el backend, que **recalcula y valida** lo que envía el frontend: área, texto de medidas y rangos. El frontend las replica solo para mostrar el plano; ambos lados se prueban con los mismos casos de ejemplo.

### 2.5 Reglas de Flyway
- Migraciones en `backend/src/main/resources/db/migration/`, con nombres `V1__descripcion.sql`, `V2__...`, etc.
- `spring.jpa.hibernate.ddl-auto=validate`: Hibernate solo verifica que las entidades coincidan con las tablas; nunca las modifica.
- **Una migración ya ejecutada nunca se edita.** Los cambios van en una migración nueva.
- Los **datos iniciales también son migraciones** (configuración del sitio, líneas destacadas, galería, 8 productos, opciones de vidrio y usuario administrador inicial). Toma los valores de la sección 5.
- Dependencias: `flyway-core` y `flyway-database-postgresql`.

### 2.6 Mejoras respecto a la versión actual
1. **Autenticación real:** tabla `admin_user`, login con JWT en cookie httpOnly, rol `ADMIN`, límite de intentos de login. Elimina el PIN.
2. **Cotizaciones con datos estructurados:** `width_cm`, `height_cm`, `area_m2`, `glass_type`, `glass_color`, `thickness_mm`, además de nombre, teléfono, producto y mensaje. Agrega:
   - estado (`NUEVA`, `CONTACTADA`, `COTIZADA`, `CERRADA`, `DESCARTADA`);
   - borrado lógico (`deleted_at`);
   - paginación, orden por fecha y filtro por estado.
3. **Opciones de vidrio** (tipos, colores con hex y si es reflectivo, espesores) como tablas editables por el administrador.
4. **Sin flujo de "publicar":** el panel escribe directo en la base y los cambios se ven al instante. Exportar e importar JSON se conserva solo como respaldo.
5. **Formulario público** con límite por IP, campo trampa anti-bots y largos máximos (sección 3.6).
6. **Corregir todas las observaciones de la sección 6.1.**

### 2.7 Plan por fases
| Fase | Alcance |
|---|---|
| **0** | Estructura del repo, `docker-compose.yml` (postgres con volumen y healthcheck), `.env.example`, proyectos Spring Boot y Vite inicializados, README con cómo levantar todo. |
| **1** | Base del backend (errores, configuración, CORS) y módulo `settings` (configuración del sitio, líneas destacadas, galería) con migraciones y semillas. |
| **2** | Módulo `catalog` (productos, especificaciones, características, opciones de vidrio) con semilla de los 8 productos. |
| **3** | Módulo `quote`: creación pública con todas las validaciones; bandeja del administrador con estados, filtros y paginación. |
| **4** | Módulo `auth`: login, JWT en cookie, protección de rutas, usuario administrador inicial. |
| **5** | Frontend conectado a la API: sitio público, configurador (sección 3.5) y panel de administración. |
| **6** | Migración de las cotizaciones existentes desde Upstash Redis (hash `vya:quotes`, id → JSON) y despliegue. |

### 2.8 Cómo quiero que trabajes
- Antes de escribir código, revisa este documento y propón el **modelo de datos** (entidades, relaciones, tipos, índices) y la **lista de endpoints** con sus DTOs. La sección 4 es un punto de partida, no una orden: mejórala si hace falta y **espera mi aprobación**.
- Avanza **una fase a la vez**. Al terminar cada una: compila, las pruebas pasan y `docker compose up` funciona.
- Commits pequeños y descriptivos.
- Explícame en español qué hiciste y por qué, y avísame si algo es ambiguo.

**Empieza por la Fase 0 y la propuesta del modelo de datos y los endpoints.**

---

## 3. Especificación funcional

> Describe **qué hace** el sistema. Cada módulo indica el comportamiento actual y, cuando cambia, el comportamiento **en la versión nueva**.

### 3.0 Visión general
El sitio cumple tres funciones:
1. **Vitrina comercial:** presenta la empresa, sus líneas de producto y un catálogo con especificaciones técnicas.
2. **Captación de clientes:** un configurador de medidas y vidrio, y un formulario de solicitud de cotización.
3. **Autogestión:** un panel de administración para editar el contenido y atender las cotizaciones.

**Actores**

| Actor | Descripción | Acceso actual | Acceso en la versión nueva |
|---|---|---|---|
| **Visitante** | Cliente potencial que navega el sitio. | Público | Público |
| **Administrador** | Persona de la empresa que edita contenido y atiende cotizaciones. | PIN único | Usuario y contraseña (JWT, rol `ADMIN`) |

### 3.1 Configuración general del sitio
Un único registro de configuración (singleton):

| Campo | Tipo | Regla / valor por defecto |
|---|---|---|
| `heroTitle` | texto | "Ventanas y Aluminios SAS". Forma el título de la página: `"{heroTitle} \| Fabricación e Instalación a Medida"`. **En la versión nueva también es el título grande de la portada** (hoy está fijo en el código). |
| `heroSubtitle` | texto | También es la meta-descripción de la página. |
| `heroImage` | URL | Imagen principal de portada. |
| `heroBadge` | texto corto | Etiqueta destacada de portada. |
| `phone` | texto | "+57 317 846 3260". Para enlaces de llamada se quitan los espacios. |
| `email` | texto | "ventanaluminio@hotmail.com". |
| `whatsappNumber` | texto (solo dígitos) | "573178463260". Si viene vacío se deriva de `phone`; siempre se normaliza a solo dígitos. |
| `experienceYears` | entero | 25. **En la versión nueva, todo "25 años" del sitio sale de este campo.** |
| `quotingEnabled` | booleano | `true` (ver 3.1.1). |
| `favicon` | URL | Logo de 4 paneles por defecto. Vacío = sin ícono. |
| `about.paragraph1` | texto | Historia de la empresa, párrafo 1. |
| `about.paragraph2` | texto | Historia de la empresa, párrafo 2. |
| `about.highlightText` | texto | Texto destacado de "Quiénes Somos". |

#### 3.1.1 Interruptor de cotización (`quotingEnabled`)
- Con `false` se ocultan el acceso a "Cotizar", los botones de cotizar del catálogo y la portada, el formulario, el configurador y el acceso directo por WhatsApp. El teléfono y el correo siguen visibles.
- Si alguien entra a la sección de cotizar con el interruptor apagado, se le redirige a Inicio.
- **En la versión nueva**, `POST /api/v1/quotes` también debe rechazar solicitudes cuando está apagado (hoy solo se oculta en la interfaz).

### 3.2 Líneas destacadas (portada)
Lista ordenable de tarjetas que llevan al catálogo filtrado por categoría.

| Campo | Tipo |
|---|---|
| `id` | identificador |
| `position` | orden |
| `number` | texto ("01", "02"…) |
| `title` | texto |
| `categoryTag` | texto |
| `description` | texto |
| `image` | URL |
| `linkText` | texto del enlace |
| `targetCategory` | `VENTANAS` \| `PUERTAS` \| `PORTONES` \| `DIVISIONES` |

**Funciones:** editar campos y reordenar (subir/bajar). Al hacer clic en una tarjeta se abre el catálogo filtrado por `targetCategory`.

### 3.3 Galería de imágenes
Lista de imágenes `{ id, title, url }` reutilizable desde cualquier selector de imagen del panel.
- Agregar una imagen por URL o subiendo un archivo; el título por defecto es "Imagen".
- Eliminar imágenes.
- Semilla: 5 imágenes (sección 5.7.3).
- Hoy la galería **no se muestra al público**. Decisión pendiente en 6.2.

### 3.4 Catálogo de productos

| Campo | Tipo | Notas |
|---|---|---|
| `id` | identificador | |
| `category` | enum | `VENTANAS`, `PUERTAS`, `PORTONES`, `DIVISIONES` |
| `categoryNumber` | texto opcional | "01".."04" |
| `title` | texto | |
| `subtitle` | texto | |
| `description` | texto | |
| `image` | URL | |
| `badge` | texto opcional | |
| `specs` | lista `{ label, value }` ordenada | Especificaciones técnicas |
| `features` | lista de texto ordenada | Ventajas / características |
| `ctaText` | texto opcional | Texto del botón de cotizar |
| `position` | orden | |
| `active` | booleano | **Nuevo:** permite ocultar un producto sin borrarlo |

**Funciones públicas**
- Listar todos los productos activos o filtrarlos por categoría.
- Ver el detalle de un producto.
- **Cotizar un producto:** lleva a Cotizar con el tipo de producto precargado como `"{title} ({Categoría con mayúscula inicial})"`.

**Funciones de administración**
- Crear, editar, duplicar, reordenar y eliminar productos.
- Agregar o quitar especificaciones y características.
- Producto nuevo por defecto: título "Nuevo producto", característica "Característica destacada 1", botón "Cotizar este sistema".

**Semilla:** los 8 productos de la sección 5.4.4.

### 3.5 Configurador de vidrio — "Plano Esquemático Dinámico"
Herramienta para que el cliente defina medidas y vidrio antes de pedir cotización.

#### 3.5.1 Parámetros

| Parámetro | Dominio | Por defecto |
|---|---|---|
| Ancho | entero 0–600 cm | 150 |
| Alto | entero 0–260 cm | 120 |
| Tipo de vidrio | Crudo, Laminado, Templado, Laminado templado, Acústico, Termo acústico | Crudo |
| Color (aplica a **todos** los tipos) | ver tabla | Transparente |
| Espesor | 4, 5, 6, 8, 10, 12, 15 mm | 6 mm |

**Colores**

| Código | Nombre | Hex | Reflectivo | Nota |
|---|---|---|---|---|
| `TRANSPARENTE` | Transparente | `#E0F2FE` (referencia) | No | |
| `BRONCE` | Bronce | `#966969` | No | |
| `AZUL_LAGO` | Azul lago | `#ABC7E1` | No | |
| `AZUL_INTENSO` | Azul intenso | `#396DC6` | No | |
| `REFLECTIVO` | Reflectivo | `#396DC6` | **Sí** | "Mismo tono que Azul intenso, con acabado reflectivo hacia el exterior." |
| `VERDE_FRENCE` | Verde Frence | `#B8FFB8` | No | Nombre tal como lo dio el cliente (ver 6.2) |
| `VERDE_INTENSO` | Verde intenso | `#2B7133` | No | |

#### 3.5.2 Reglas de negocio
- **Ajuste de medidas:** se redondea al entero y se limita a [0, máximo]; los valores no numéricos pasan a 0. Los botones −/+ cambian de 5 en 5 cm.
- **Área:** `área_m2 = ancho_cm × alto_cm / 10000`, con 2 decimales.
- **Hojas:** `hojas = max(1, ceil(ancho / 150))`; con 600 cm salen 4. *(Decisión pendiente en 6.2.)*
- **Descripción del vidrio:** `"Vidrio {tipo en minúscula} {color en minúscula} {espesor} mm"`.
- **Texto de medidas:** `"{alto} cm alto x {ancho} cm ancho ({área} m²) · {descripción del vidrio}"`.
- **Aplicar a la cotización:** solo si ancho > 0 y alto > 0. Llena el campo *Medidas aproximadas* y genera el mensaje sugerido.
- **Mensaje sugerido:** `"Hola, me interesa una cotización para {tipo de producto} de {ancho} cm x {alto} cm en {descripción del vidrio con inicial minúscula}."`
- **Sincronización:** después del primer "Aplicar", cada cambio de medidas, tipo, color o espesor, y del *tipo de producto* del formulario, actualiza las medidas y el mensaje. Con una medida en 0 se conservan los últimos valores válidos.
- **Combinación con el texto del cliente:**
  1. Si el mensaje está vacío, se pone el sugerido.
  2. Si contiene el sugerido anterior, se reemplaza solo esa parte.
  3. Si ya contiene el nuevo sugerido, no se toca.
  4. En otro caso, el sugerido va al inicio, seguido de un salto de línea y el texto del cliente.
- **Visual (solo frontend):** el plano se dibuja a escala real, con la misma escala en ancho y alto, para que 600 × 260 cm quepa en el lienzo. La escala mostrada es "ESC ≈ 1:N" según la pantalla, y las cotas miden lo mismo que la ventana.

### 3.6 Solicitud de cotización (público)

**Campos**

| Campo | Obligatorio | Largo máx. | Notas |
|---|---|---|---|
| `name` | Sí | 100 | Nombre y apellido |
| `phone` | Sí | 40 | Teléfono / WhatsApp |
| `productType` | No | 120 | Opciones en 5.6.4. Por defecto "Ventanas arquitectónicas", o el producto elegido en el catálogo. |
| `dimensionsText` | No | 200 | Texto libre, o el generado por el configurador |
| `message` | No | 2000 | Texto libre, o el sugerido por el configurador |
| `widthCm`, `heightCm`, `glassType`, `glassColor`, `thicknessMm` | No | — | **Nuevos:** datos estructurados del configurador. Si vienen, el servidor valida los rangos y opciones y recalcula `areaM2` y `dimensionsText`. |
| `website` | — | — | Campo trampa anti-bots, oculto; debe llegar vacío |

**Reglas al recibir una solicitud**
1. Si `website` trae contenido, se responde éxito pero **no se guarda**.
2. Se recortan los espacios de los campos de texto.
3. `name` o `phone` vacíos → **400** "Nombre y teléfono son obligatorios."
4. **Límite por IP:** máximo 10 envíos por hora → **429** "Has enviado muchas solicitudes. Intenta más tarde."
5. Si `quotingEnabled = false` → rechazar (nuevo).
6. Se guarda con `id`, `createdAt` (UTC), estado `NUEVA` e IP de origen.
7. Respuesta **201** con el `id`.

> El sitio actual tiene un tope global de 2000 cotizaciones por el límite de Redis. **En PostgreSQL no hace falta;** se reemplaza por el límite por IP y la paginación.

**Comportamiento para el visitante**
- Éxito: "¡Cotización enviada!", y se limpian nombre, teléfono y mensaje.
- Error: se muestra el mensaje del servidor junto con el teléfono y el correo de la empresa.

### 3.7 Bandeja de cotizaciones (administrador)
- **Hoy:** lista completa de la más antigua a la más reciente, contador, actualizar y eliminar definitivo con confirmación.
- **En la versión nueva:**
  - lista paginada, ordenable por fecha (por defecto de la más antigua a la más reciente) y filtrable por estado;
  - cada cotización muestra fecha y hora, nombre, teléfono (enlace para llamar), tipo de producto, medidas (texto y datos estructurados), vidrio y mensaje;
  - cambio de estado: `NUEVA` → `CONTACTADA` → `COTIZADA` → `CERRADA` / `DESCARTADA`;
  - **borrado lógico** (`deleted_at`), con confirmación;
  - contador de cotizaciones `NUEVA` visible al abrir el panel.

### 3.8 Contacto
- Enlaces de llamada (`tel:`), correo (`mailto:`) y WhatsApp (`https://wa.me/{dígitos}?text=...`).
- Mensaje predeterminado de WhatsApp: "Hola Ventanas y Aluminios, quiero cotizar un proyecto a medida."
- El administrador edita teléfono, correo y número de WhatsApp.

### 3.9 Autenticación del administrador
- **Hoy:** se entra con `?admin=1` y un PIN único hasheado. Tiene bloqueo de 5 intentos / 60 s y la sesión se guarda en el navegador.
- **En la versión nueva:**
  - ruta `/admin/login` con correo y contraseña (BCrypt);
  - JWT en cookie httpOnly y rol `ADMIN`;
  - límite de intentos de login por IP (con el mismo filtro de 3.6);
  - logout que invalida la cookie;
  - usuario inicial creado por migración, con contraseña tomada de una variable de entorno o forzada a cambiar en el primer ingreso (proponer).

### 3.10 Respaldo de contenido
- **Hoy:** el contenido se guarda en el navegador y se "publica" descargando `published.json` y volviendo a desplegar.
- **En la versión nueva:** el panel escribe directo en la base. Se conserva:
  - **Exportar respaldo:** JSON con `{ version, exportedAt, content, products }`;
  - **Importar respaldo:** valida que sea JSON, que exista `content` y que `products` sea una lista; reemplaza con confirmación.
  - Mensajes: "El archivo no es un JSON válido." · 'El archivo no parece un respaldo del sitio (falta "content").' · 'El respaldo tiene un catálogo inválido ("products" debe ser una lista).'

### 3.11 Imágenes
- **Hoy:** el navegador redimensiona las imágenes (máx. 1600 px, ≤ 400 KB) y las guarda como base64.
- **En la versión nueva:** se guarda la **URL** de la imagen, o el archivo en una carpeta del servidor montada como volumen. Límite de tamaño y solo tipos de imagen (proponer el límite). Nunca base64 en la base.

### 3.12 SEO
- El título de la pestaña y la meta-descripción salen de `heroTitle` y `heroSubtitle`.
- `og:title`, `og:description`, `og:image` y `og:locale` (`es_CO`) están fijos (sección 5.1).

---

## 4. Modelo de datos y endpoints (punto de partida)

> Propuesta inicial para revisar y mejorar en la Fase 0. **No es definitiva.**

### 4.1 Tablas

| Tabla | Columnas principales |
|---|---|
| `site_settings` (1 fila) | `id`, `hero_title`, `hero_subtitle`, `hero_image_url`, `hero_badge`, `phone`, `email`, `whatsapp_number`, `experience_years`, `quoting_enabled`, `favicon_url`, `about_paragraph1`, `about_paragraph2`, `about_highlight`, `updated_at` |
| `featured_line` | `id`, `position`, `number`, `title`, `category_tag`, `description`, `image_url`, `link_text`, `target_category` |
| `gallery_image` | `id`, `position`, `title`, `url`, `created_at` |
| `product` | `id`, `category`, `category_number`, `title`, `subtitle`, `description`, `image_url`, `badge`, `cta_text`, `position`, `active`, `created_at`, `updated_at` |
| `product_spec` | `id`, `product_id` (FK), `position`, `label`, `value` |
| `product_feature` | `id`, `product_id` (FK), `position`, `text` |
| `glass_type` | `id`, `code`, `name`, `position`, `active` |
| `glass_color` | `id`, `code`, `name`, `hex`, `reflective`, `note`, `position`, `active` |
| `glass_thickness` | `id`, `mm`, `position`, `active` |
| `quote` | `id`, `created_at`, `name`, `phone`, `product_type`, `width_cm`, `height_cm`, `area_m2`, `glass_type_code`, `glass_color_code`, `thickness_mm`, `dimensions_text`, `message`, `status`, `source_ip`, `updated_at`, `deleted_at` |
| `admin_user` | `id`, `email` (único), `password_hash`, `role`, `active`, `last_login_at`, `created_at` |

Enums: `product_category` = `VENTANAS`, `PUERTAS`, `PORTONES`, `DIVISIONES`; `quote_status` = `NUEVA`, `CONTACTADA`, `COTIZADA`, `CERRADA`, `DESCARTADA`.

Índices sugeridos: `quote(status, created_at)`, `quote(deleted_at)`, `product(category, position)`.

### 4.2 Endpoints

**Públicos**

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/v1/site` | Configuración pública más las líneas destacadas |
| `GET` | `/api/v1/products?category=` | Productos activos, opcionalmente filtrados |
| `GET` | `/api/v1/products/{id}` | Detalle de un producto |
| `GET` | `/api/v1/glass-options` | Tipos, colores y espesores activos |
| `POST` | `/api/v1/quotes` | Crear una cotización (3.6) |

**Autenticación**

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Login; deja el JWT en cookie httpOnly |
| `POST` | `/api/v1/auth/logout` | Cierra la sesión |
| `GET` | `/api/v1/auth/me` | Usuario actual |

**Administración** (rol `ADMIN`)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` / `PUT` | `/api/v1/admin/settings` | Ver / actualizar la configuración |
| `GET` / `POST` / `PUT` / `DELETE` | `/api/v1/admin/featured-lines[/{id}]` | CRUD de líneas destacadas |
| `PUT` | `/api/v1/admin/featured-lines/order` | Reordenar |
| `GET` / `POST` / `DELETE` | `/api/v1/admin/gallery[/{id}]` | Galería |
| `GET` / `POST` / `PUT` / `DELETE` | `/api/v1/admin/products[/{id}]` | CRUD de productos (con specs y features) |
| `POST` | `/api/v1/admin/products/{id}/duplicate` | Duplicar |
| `PUT` | `/api/v1/admin/products/order` | Reordenar |
| CRUD | `/api/v1/admin/glass-types`, `/glass-colors`, `/glass-thicknesses` | Opciones de vidrio |
| `GET` | `/api/v1/admin/quotes?status=&page=&size=&sort=` | Bandeja paginada |
| `GET` | `/api/v1/admin/quotes/{id}` | Detalle |
| `PATCH` | `/api/v1/admin/quotes/{id}/status` | Cambiar estado |
| `DELETE` | `/api/v1/admin/quotes/{id}` | Borrado lógico |
| `GET` / `POST` | `/api/v1/admin/backup` | Exportar / importar respaldo |

**Formato de error:** `{ "error": "mensaje en español", "code": "CODIGO" }`. Códigos HTTP: 400 (validación), 401 (no autenticado), 403 (sin permiso), 404, 409, 429 (límite), 500.

---

## 5. Contenido textual del sitio

> Inventario de todo el texto del sitio actual, por sección.
> **Leyenda:**
> - **[E: `campo`]** — editable desde el panel (va a la base de datos);
> - **[F]** — texto fijo de la interfaz;
> - **[D]** — se calcula a partir de otros datos;
> - *(si cotizar está activo)* — solo con `quotingEnabled = true`.
>
> En la versión nueva hay que aplicar las correcciones de la sección 6.1.

### 5.0 Datos generales de la empresa

| Dato | Valor | Origen |
|---|---|---|
| Nombre comercial | Ventanas y Aluminios SAS | [E: `heroTitle`] |
| Teléfono | +57 317 846 3260 | [E: `phone`] |
| Correo | ventanaluminio@hotmail.com | [E: `email`] |
| WhatsApp | 573178463260 | [E: `whatsappNumber`] |
| Años de experiencia | 25 | [E: `experienceYears`] |
| Mensaje predeterminado de WhatsApp | "Hola Ventanas y Aluminios, quiero cotizar un proyecto a medida." | [F] |

### 5.1 Metadatos de la página

| Elemento | Texto | Origen |
|---|---|---|
| Título de la pestaña | "{heroTitle} \| Fabricación e Instalación a Medida" | [D] |
| Meta descripción | = `heroSubtitle` | [D] |
| og:title | "Ventanas y Aluminios \| Fabricación e Instalación a Medida" | [F] |
| og:description | "Fabricación e instalación a medida de ventanas, puertas, portones y divisiones con acabados de excelencia y más de 25 años de experiencia." | [F] |
| og:locale | es_CO | [F] |
| Favicon | Logo de 4 paneles (cuadro azul con 4 ventanitas) | [E: `favicon`] |

### 5.2 Elementos presentes en todas las páginas

#### 5.2.1 Barra superior (cabecera)
- Franja: **"FÁBRICA · INSTALACIÓN · A MEDIDA"** [F] / correo [E] / **"25+ AÑOS"** / teléfono [E].
- Marca: **"Ventanas y Aluminios"** [F].
- Nombre de la sección actual [F]: `INICIO` · `CATÁLOGO` · `NOSOTROS` · `COTIZAR` · `PANEL DE CONTROL`.
- Botón **"Cotizar Proyecto"** [F] *(si cotizar está activo)*.
- Accesibilidad: "Abrir menú de navegación", "Llamar".

#### 5.2.2 Menú lateral
- Marca: **"Ventanas y Aluminios"** / **"FABRICACIÓN A MEDIDA"** [F].
- Distintivo: **"25+ AÑOS DE CONFIANZA"** · **"Norma NSR"** [F].
- **"NAVEGACIÓN"**:

  | Opción | Subtexto |
  |---|---|
  | Inicio | — |
  | Catálogo | "4 Líneas" |
  | Nosotros | — |
  | Cotizar *(si cotizar está activo)* | "Plano Escala" |

- Tarjeta *(si cotizar está activo)*: **"CONFIGURADOR A ESCALA"** — "Calcula medidas milimétricas de tu ventana o puerta en tiempo real." — botón **"Abrir Configurador"** [F].
- **"WHATSAPP OFICIAL"** — "Respuesta Inmediata" [F].
- Contacto: teléfono [E], correo [E], "Taller de carpintería y ensamble" [F].
- Accesibilidad: "Cerrar menú".

#### 5.2.3 Navegación inferior (celular)
Inicio · Catálogo · Nosotros · Cotizar *(si cotizar está activo)* [F].

#### 5.2.4 Pie de página
- Marca: **"Ventanas y Aluminios"** / **"FABRICACIÓN A MEDIDA"** [F].
- "Soluciones en vidrio, aluminio y otros materiales, fabricadas e instaladas con más de 25 años de experiencia." [F]
- "Garantía estructural & cortes milimétricos" [F].
- **"SECCIONES"**: Inicio · Catálogo de Sistemas · ¿Quiénes somos? · Configurador & Cotizar *(si cotizar está activo)* [F].
- **"TALLER & ATENCIÓN"**: correo [E], teléfono [E], "Instalaciones en toda el área metropolitana y proyectos a nivel regional." [F]
- "© Ventanas y Aluminios. Fabricación e instalación a medida." · "Todos los derechos reservados · Acabados de Excelencia" [F].

### 5.3 Inicio

#### 5.3.1 Portada (hero)
| Elemento | Texto | Origen |
|---|---|---|
| Etiqueta superior | "FÁBRICA · INSTALACIÓN · A MEDIDA" | [F] |
| Título | "Ventanas y Aluminios SAS" | [E: `heroTitle`] *(hoy fijo, ver 6.1)* |
| Subtítulo | "Fabricamos e instalamos ventanas, puertas, portones y divisiones, con más de 25 años de experiencia y acabados de excelencia." | [E: `heroSubtitle`] |
| Botones | "Ver catálogo" · "Cotizar" *(si cotizar está activo)* | [F] |
| Viñetas | "Perfiles alta resistencia" · "Vidrio de seguridad templado" | [F] |
| Imagen | Fachada de aluminio y vidrio (alt: "Ventanas y fachadas de aluminio y vidrio") | [E: `heroImage`] |
| Etiqueta sobre la imagen | "VIDRIO TEMPLADO & ALUMINIO · Sistemas 2025" | [E: `heroBadge`] |
| Tarjeta flotante | "INGENIERÍA ESTRUCTURAL" — "Calibre Europeo & Sellado Térmico" | [F] |
| Indicador | "DESLIZA PARA CONOCER MÁS" | [F] |

#### 5.3.2 Trayectoria
- **"NUESTRA TRAYECTORIA"** [F] · **"{experienceYears} años fabricando confianza"** [D].
- Tarjetas [F]:
  - "DESDE NUESTRO TALLER"
  - "AJUSTE TÉCNICO" — **"Medida Milimétrica"** — "PRECISIÓN MILIMÉTRICA" *(hoy dice "PRESCISIÓN MILIMETRICA", ver 6.1)*
  - "GARANTÍA" — **"Instalación Limpia"** — "Personal técnico especializado que respeta tu obra, protegiendo pisos, acabados y dejando el área impecable."
- Botón: "Conócenos" [F].

#### 5.3.3 Líneas especializadas
- **"LÍNEAS ESPECIALIZADAS"** · **"Lo que fabricamos"** [F].
- "Cuatro líneas de trabajo y una sola obsesión: medidas exactas, instalación limpia y acabados que duren." [F]
- Tarjetas [E: `featured_line`] — semilla:

| N.º | Etiqueta de categoría | Título | Descripción | Enlace | Categoría destino |
|---|---|---|---|---|---|
| 01 | SISTEMAS ACÚSTICOS & TÉRMICOS | Ventanas | Corredizas y batientes en aluminio, con vidrio sencillo, doble o templado. | Ver productos | VENTANAS |
| 02 | SEGURIDAD & ACCESO RESIDENCIAL | Puertas | De aluminio, vidrio y PVC, para interiores, exteriores y proyectos comerciales. | Ver productos | PUERTAS |
| 03 | SISTEMAS AUTOMÁTICOS & MANUALES | Portones | Instalación de portones tipo americano, robustos y de operación confiable. | Ver productos | PORTONES |
| 04 | ESPACIOS INTERIORES & BAÑOS | Divisiones | En vidrio templado y acrílico para baños y oficinas, fabricadas a la medida exacta. | Ver productos | DIVISIONES |

#### 5.3.4 Llamado final *(si cotizar está activo)*
- "ATENCIÓN PERSONALIZADA" — **"¿Listo para tu proyecto? Recibimos tu cotización."** [F]
- "Medidas, obras y proyectos residenciales, comerciales y empresariales." [F]
- Botón **"Cotizar ahora"** · "Respuesta técnica en menos de 24 horas" [F].

### 5.4 Catálogo

#### 5.4.1 Encabezado
- **"CATÁLOGO ARQUITECTÓNICO"** · **"Cuatro líneas de trabajo con medida exacta"** [F].
- "Cada producto se fabrica para la medida de tu obra. Explora nuestros sistemas, tipos y acabados diseñados para durabilidad estructural y elegancia." [F]

| Etiqueta | Valor | Detalle |
|---|---|---|
| PRECISIÓN | ± 1.0 mm | Ajuste perimetral |
| ACABADOS | Anodizado | Y pintura electrostática |
| GARANTÍA | +{experienceYears} Años | De experiencia real |

- Filtros [F]: **Todos los Sistemas** · Ventanas · Puertas · Portones · Divisiones.

#### 5.4.2 Tarjeta de producto
- Número de categoría · etiqueta **"GALERÍA DE TRABAJOS"** (hoy igual en todas, ver 6.1) · distintivo · título · subtítulo · descripción · 3 especificaciones ("ETIQUETA: valor").
- Botón con `ctaText` *(si cotizar está activo)* · botón **"Detalles"** [F].

#### 5.4.3 Ventana de detalle
- Categoría · **"{título} - {subtítulo}"** · descripción · imagen.
- **"Ficha Técnica & Características"** [F] · **"Ventajas del Sistema:"** [F].
- Botón **"Solicitar Cotización de este Sistema"** *(si cotizar está activo)*.
- Accesibilidad: "Cerrar modal".

#### 5.4.4 Productos (semilla) [E: catálogo]

| # | Categoría | Título | Subtítulo | Distintivo | Descripción |
|---|---|---|---|---|---|
| 1 | Ventanas (01) | Corredizas | Línea Panorámica | 4 Obras instaladas | Ideal para espacios amplios: aperturas suaves, buena ventilación y máxima luz natural. |
| 2 | Ventanas (01) | Batientes | Apertura 90° con freno | Apertura 90° con freno | Hojas abatibles con sellado hermético. Un clásico funcional para hogares y oficinas con aislamiento termoacústico. |
| 3 | Puertas (02) | Aluminio | Línea Pesada & Fachadas | Línea Pesada | Resistentes a la intemperie y de bajo mantenimiento, para interiores y exteriores residenciales y comerciales. |
| 4 | Puertas (02) | Vidrio | Templado Monumental & Pivote | Templado 8mm - 12mm | Puertas de vidrio templado, elegantes y seguras, para interiores de oficina y entradas principales de diseño arquitectónico. |
| 5 | Puertas (02) | PVC & Termoacústicas | Máximo Aislamiento | Aislamiento Térmico | Buen aislamiento térmico y acústico para hogares, oficinas y espacios comerciales que requieren confort climático total. |
| 6 | Portones (03) | Instalación · Americanos | Robustez & Automatización | Herrajes de alta resistencia | Instalación de portones tipo americano: robustos, duraderos y con herrajes confiables para residencias y empresas. |
| 7 | Divisiones (04) | Vidrio Templado | Baños Master & Oficinas | Espesor 8mm a 10mm | Separadores elegantes, luminosos y seguros, fabricados a la medida de tu espacio con herrajes en acero inoxidable. |
| 8 | Divisiones (04) | Acrílico & Policarbonato | Económicas y Resistentes | Antigolpes y Ligero | Divisiones livianas, económicas y resistentes al agua, perfectas para baños familiares y áreas de alto tránsito. |

| # | Especificaciones (etiqueta: valor) | Ventajas / características | Texto del botón |
|---|---|---|---|
| 1 | Hojas: 2, 3 o 4 hojas deslizantes · Hermeticidad: Felpa siliconada perimetral · Vidrios compatibles: Sencillo, doble, templado | Rodamientos de alta precisión en acero inox · Felpa siliconada cortaviento · Seguro embutido multipunto | Ver galería / Solicitar muestra |
| 2 | Aislamiento: Acústico superior · Cierre: Punto múltiple hermético · Perfilería: Aluminio extruido calibre pesado | Brazos de fricción telescópicos · Sellado perimetral con empaque EPDM · Manija ergonómica europea | Cotizar sistema batiente |
| 3 | Resistencia: Resistente a Corrosión y Salitre · Seguridad: Cerraduras de seguridad multipunto · Bisagras: Bisagras en acero | Perfil estructural serie 80 · Cerradura de alta seguridad · Acabados anodizados o pintura electrostática | Cotizar puerta de aluminio |
| 4 | Cristal: Templado 8mm - 12mm de seguridad · Mecanismo: Pivotes hidráulicos de piso · Acabados: Vidrio Satinado o Claro | Cajas hidráulicas embutidas al piso · Tiradores arquitectónicos en acero inoxidable · Freno a 90° con retorno controlado | Cotizar puerta de vidrio templado |
| 5 | Clima: Cero condensación y sellado total · Cierres: Cierres herméticos perimetrales · Refuerzo: Almatensor en casos necesarios | Perfiles multicámara europeos · Vidrio laminado acústico o termopanel · Ahorro de energía en climatización | Cotizar puerta termoacústica |
| 6 | Accionamiento: Accionamiento suave con motor silencioso · Seguridad: Seguridad reforzada con bloqueo automático · Estructura: Paneles térmicos inyectados de poliuretano | Control remoto y apertura por app · Sensores de reversa infrarrojos · Resortes de torsión de alto ciclo | Cotizar portón seccional |
| 7 | Aplicación: Para baños master & oficinas · Espesor: Cristal templado 8mm a 10mm · Herrajes: Acero inoxidable 304 anti-sarro | Sellado magnético anti-fugas de agua · Tratamiento hidrófugo fácil limpieza · Cortes y perforaciones exactas por láser | Cotizar división en vidrio templado |
| 8 | Acabados: Diseños lisos, gota de agua y texturizados · Ventajas: Antigolpes y peso ultra-ligero · Marco: Aluminio anodizado blanco, natural o negro | Perfilería de aluminio de rápida instalación · Placas resistentes a la humedad constante · Económico y duradero | Cotizar división en acrílico |

> Las URLs de las imágenes de cada producto están en `src/data/initialData.ts` del repo viejo.

#### 5.4.5 Llamado final
- "FABRICACIÓN ESPECIALIZADA" — **"¿Necesitas un sistema específico? Te asesoramos."** [F]
- "Cuéntanos el espacio y las medidas, y te recomendamos el producto y calibre indicado." [F] *(hoy dice "Contanos", ver 6.1)*
- Botón **"Cotizar ahora"** *(si cotizar está activo)*.

### 5.5 Quiénes somos

#### 5.5.1 Encabezado
- **"¿QUIÉNES SOMOS?"** · **"Una empresa que mide todo, menos su dedicación"** [F].
- "Fabricación e instalación de soluciones en vidrio, aluminio y otros materiales desde hace más de {experienceYears} años." [D]
- **"{experienceYears}+ años"** [D] — "SOLIDEZ & CONFIANZA" · "Garantía en Obra" [F].
- Pie de foto: "TALLER DE CARPINTERÍA Y ENSAMBLE A MEDIDA" [F] (alt: "Taller de carpintería y ensamble a medida de Ventanas y Aluminios").

#### 5.5.2 Nuestra historia
- **"NUESTRA HISTORIA"** · **"¿Quiénes somos?"** [F].
- [E: `about.paragraph1`]: "Somos una empresa especializada en la fabricación e instalación de soluciones en vidrio, aluminio y otros materiales, con más de 25 años de experiencia en el sector."
- [E: `about.paragraph2`]: "Durante todos estos años hemos trabajado para brindar a nuestros clientes productos de alta calidad, excelentes acabados y soluciones adaptadas a sus necesidades, cuidando cada detalle desde la fabricación hasta la instalación."
- [E: `about.highlightText`]: "Contamos con un equipo de trabajo comprometido y con amplia experiencia, preparado para desarrollar proyectos residenciales, comerciales y empresariales."

#### 5.5.3 Nuestra experiencia
- **"NUESTRA EXPERIENCIA"** · **"Más de {experienceYears} años fortaleciendo procesos"** [D].
- "Más de {experienceYears} años de trayectoria nos han permitido fortalecer nuestros conocimientos, mejorar nuestros procesos y ofrecer trabajos confiables y duraderos." [D]
- Contador **"{experienceYears} AÑOS"** — "Años de experiencia" [D].

| Título | Subtítulo | Texto [F] |
|---|---|---|
| Trayectoria consolidada | — | Fabricando e instalando soluciones arquitectónicas de alta gama con precisión europea. |
| Proyectos integrales | Alcance versátil | Atención a proyectos residenciales, comerciales y empresariales con garantías reales. |
| Equipo especializado | Sin intermediarios | Fabricación e instalación con personal técnico propio certificado en seguridad de obra. |

#### 5.5.4 Nuestro compromiso
- **"NUESTRO COMPROMISO"** · **"Calidad en cada entrega"** [F].
- "Nos comprometemos con cada proyecto, buscando siempre entregar calidad, responsabilidad, cumplimiento y excelentes acabados." [F]

| Título | Texto [F] |
|---|---|
| Medidas exactas | Precisión milimétrica en vanos, perfiles y vidrios templados para un acople hermético y perfecto. |
| Instalación limpia | Procedimientos rigurosos en obra que cuidan tu espacio, con sellados pulcros y protección de áreas. |
| Garantía comprobada | Soporte continuo y materiales certificados que aseguran máxima resistencia acústica y climática. |

#### 5.5.5 Llamado final *(si cotizar está activo)*
- "INICIA TU PROYECTO" — **"¿Quieres un trabajo así de cuidado? Hablemos."** [F]
- "Te contamos cómo fabricamos e instalamos cada proyecto a la medida exacta de tu hogar u oficina." [F]
- Botón **"Cotizar ahora"**.

### 5.6 Cotizar *(toda la sección requiere que cotizar esté activo)*

#### 5.6.1 Encabezado
- **"COTIZAR"** · **"Hablemos de tu proyecto"** [F].
- "Escríbenos o llámanos: te asesoramos en materiales, medidas y presupuesto con tolerancias exactas y garantía de fábrica." [F]

#### 5.6.2 Canales de contacto
- "CONTÁCTANOS" — **"Cotiza sin costo y sin compromiso"** [F].
- "Cuéntanos el espacio y las medidas, y te recomendamos la mejor opción en vidrio, aluminio o PVC para tu hogar, tu oficina o tu negocio." [F]
- Botón **"Escribir por WhatsApp"** — "RESPUESTA RÁPIDA · CANAL DIRECTO" [F].
- "CORREO ELECTRÓNICO": correo [E] · "TELÉFONO · WHATSAPP": teléfono [E].

#### 5.6.3 Plano Esquemático Dinámico
- **"Plano Esquemático Dinámico"** · **"CONFIGURADOR DIMENSIONAL A ESCALA"** [F].
- **"ESC ≈ 1:{N}"** [D] (ayuda: "Escala aproximada del dibujo en esta pantalla").
- Cotas "{ancho} cm" y "{alto} cm" · etiqueta interna "{TIPO} · {espesor} MM" (se oculta si no cabe) [D].
- Tarjetas **ANCHO** · **ALTO** · **ÁREA ESTIMADA** "{área} m²" [D].
- Resumen: "Vidrio {tipo} · {Color} · {espesor} mm" + " · {n} hojas" cuando hay más de una [D].
- Con una medida en 0: "Ajusta el ancho y el alto (mayores a 0 cm) para ver el plano." [F]

| Etiqueta | Opciones / rango | Ayudas |
|---|---|---|
| ANCHO DE VENTANA (CM) | 0 – 600 cm | "Rango: 0 – 600 cm"; accesibilidad: "Disminuir ancho", "Aumentar ancho", "ancho en centímetros" |
| ALTO DE VENTANA (CM) | 0 – 260 cm | "Rango: 0 – 260 cm"; accesibilidad: "Disminuir alto", "Aumentar alto", "alto en centímetros" |
| TIPO DE VIDRIO: | Crudo · Laminado · Templado · Laminado templado · Acústico · Termo acústico | — |
| COLOR DEL VIDRIO: | Transparente · Bronce · Azul lago · Azul intenso · Reflectivo · Verde Frence · Verde intenso | Con Reflectivo: "Mismo tono que Azul intenso, con acabado reflectivo hacia el exterior." |
| ESPESOR: | 4 · 5 · 6 · 8 · 10 · 12 · 15 mm | — |

- Botón **"Aplicar medidas a mi cotización"** [F].
  - Con una medida en 0: "El ancho y el alto deben ser mayores a 0 cm para aplicarlos."
  - Al aplicar: "✓ Medidas aplicadas al formulario de cotización."

#### 5.6.4 Solicitud de Cotización Rápida
- **"FORMULARIO DIRECTO"** · **"Solicitud de Cotización Rápida"** [F].
- "Completa tus datos y recibirás asesoría técnica detallada con cálculo de materiales." [F]

| Etiqueta | Tipo | Placeholder / ayuda |
|---|---|---|
| NOMBRE Y APELLIDO * | texto, obligatorio | Ej. Carlos Mendoza |
| TELÉFONO / WHATSAPP * | teléfono, obligatorio | +57 300 000 0000 |
| TIPO DE PRODUCTO | lista | (opciones abajo) |
| MEDIDAS APROXIMADAS (ALTO X ANCHO EN CM) | texto | Ej. 120 alto x 150 ancho · "💡 Tip: Puedes usar el calculador dimensional de la izquierda para fijar medidas milimétricas." |
| MENSAJE O DETALLES DEL PROYECTO | texto largo | Tipo de vidrio (templado, laminado), color de perfil o dirección aproximada... |

**Opciones de "Tipo de producto"**

| Texto visible | Valor guardado |
|---|---|
| Ventanas arquitectónicas (Corredizas / Batientes) | Ventanas arquitectónicas |
| Puertas en aluminio, vidrio o PVC | Puertas en aluminio o vidrio |
| Portones tipo americano automatizados | Portones tipo americano |
| Divisiones en vidrio templado o acrílico | Divisiones de baño o oficina |
| Fachadas flotantes o vitrinas comerciales | Fachadas flotantes o vitrinas |

**Textos generados por el configurador** [D]
- Medidas: "{alto} cm alto x {ancho} cm ancho ({área} m²) · Vidrio {tipo} {color} {espesor} mm"
- Mensaje: "Hola, me interesa una cotización para {tipo de producto} de {ancho} cm x {alto} cm en vidrio {tipo} {color} {espesor} mm."

**Botón y respuestas**
- **"Enviar cotización"** / "Enviando…"
- Éxito: **"¡Cotización enviada!"** — "Recibimos tu solicitud. Nos pondremos en contacto contigo a la brevedad al número que indicaste." — "Enviar otra cotización".
- Faltan datos: "Por favor ingresa tu nombre y teléfono para recibir la cotización."
- Error: "{mensaje del servidor} También puedes llamarnos al {teléfono} o escribir a {correo}."

#### 5.6.5 Asesoría técnica
- **"Asesoría técnica y visita a domicilio"** [F].
- "Realizamos visitas técnicas para toma de medidas exactas, muestreo físico de perfiles de aluminio y revisión estructural en sitio sin compromiso adicional." [F]
- "**Lunes a Viernes:** 7:30 AM – 6:00 PM" [F].
- "Instaladores certificados con protocolos de seguridad en obra" [F].

### 5.7 Panel de administración

> Textos del panel actual. En la versión nueva se adaptan al login con usuario y contraseña, y a escribir directo en la base. Ya no aplican "Guardar en este navegador", "Publicar" ni el PIN.

#### 5.7.1 Acceso
- **Hoy:** "Panel de Control" — "Ingresa el PIN de acceso para editar el sitio." — "PIN de acceso" — **"Entrar al Panel"** — "Volver al sitio".
- **En la versión nueva:** formulario de correo y contraseña. Mensajes sugeridos: "Correo o contraseña incorrectos." · "Demasiados intentos. Espera un minuto." · "Tu sesión expiró, vuelve a ingresar."

#### 5.7.2 Encabezado
- "PANEL DE CONTROL" · "Ver Sitio" · "Cerrar sesión".
- Indicadores: **PRODUCTOS EN CATÁLOGO** {n} · **IMÁGENES EN GALERÍA** {n} · **COTIZACIONES NUEVAS** {n}.
- Pestañas: Inicio · Catálogo · Quiénes Somos · Cotizaciones ({n}) · Configuración.
- Guardado: **"Guardar cambios"** / "Guardando…" · "¡Guardado con éxito!" · "Ocurrió un error al guardar. Intenta de nuevo."

#### 5.7.3 Pestaña Inicio
- **"Hero Principal & Slogan"** — "Edita los textos que ven los usuarios al ingresar al sitio". Campos: TÍTULO DE PORTADA · SUBTÍTULO / BAJADA · BADGE DEL BANNER SUPERIOR.
- **"Imagen principal"** — "RECOMENDADO 16:9" · "PREVISUALIZACIÓN (16:9)". Acciones: "Restablecer", "Limpiar", "Usar como imagen principal".
- **"Galería de Imágenes"** — "Agrega todas las imágenes que quieras; podrás usarlas desde cualquier selector."
  - Campos "Título (ej. Fachada comercial)" y "URL de la imagen"; botones "Subir" y "Agregar".
  - Lista vacía: "La galería está vacía." · acción "Eliminar imagen".
  - Semilla: Fachada Minimalista Cristales Altos · Ventanal Panorámico Sala Europea · Puerta Monumental & Aluminio Negro · Oficina Cristal Templado & Fachada · Taller de Carpintería y Ensamble *(URLs en `initialData.ts`)*.
- **"Tarjetas de Línea Destacada"** — "Reorganiza o actualiza los textos e imagen de cada línea de fabricación".
  - Acciones: "Editar" / "Listo", "Subir", "Bajar".
  - Campos: Título · Etiqueta de categoría · Descripción · Categoría destino · Imagen de la tarjeta.

#### 5.7.4 Pestaña Catálogo
- **"Catálogo de Productos"** — "Agrega, edita, duplica o elimina productos y sus imágenes." — "{n} PRODUCTOS".
- Filtros: Todos · Ventanas · Puertas · Portones · Divisiones. Con un filtro activo: "Agregar en {Categoría}".
- Cada fila: "{CATEGORÍA} · {n} SPECS" y acciones Subir, Bajar, Editar, Duplicar, Eliminar.
- Editor:
  - Campos: Título · Subtítulo · Categoría · Badge · Descripción · Texto del botón · Imagen del producto.
  - "Ficha Técnica (specs)" con "Etiqueta" y "Valor", y botón "Agregar spec".
  - "Ventajas / características" con botón "Agregar".
  - Botones "Listo" y "Descartar cambios".
- Confirmación: "¿Eliminar este producto del catálogo?" · Lista vacía: "No hay productos en esta categoría."

#### 5.7.5 Pestaña Quiénes Somos
- **"Quiénes Somos"** — "Textos de la sección de historia y trayectoria."
- Campos: AÑOS DE EXPERIENCIA · PÁRRAFO 1 · HISTORIA · PÁRRAFO 2 · HISTORIA · DESTACADO (RECUADRO AZUL).

#### 5.7.6 Pestaña Cotizaciones
- **"Cotizaciones"** — 'Solicitudes enviadas desde "Solicitud de Cotización Rápida".' — "Actualizar".
- Cada cotización: "#{n} · {fecha y hora}", nombre, teléfono · **TIPO DE PRODUCTO** · **MEDIDAS APROXIMADAS** · **VIDRIO** · **MENSAJE O DETALLES** ("—" si falta) · **ESTADO** (nuevo).
- Estados de la lista: "Cargando cotizaciones…" · "Aún no hay cotizaciones."
- Confirmación: "¿Eliminar la cotización de {nombre}?"
- Errores: "No se pudieron cargar las cotizaciones." · "No hay conexión. Revisa tu internet e intenta de nuevo."

#### 5.7.7 Pestaña Configuración
- **"Configuración General"** — "Controla lo que se muestra a los usuarios del sitio."
- **"Activar opción de cotización"** — 'Al desactivarla se ocultan: el menú "Cotizar", los botones de cotización, el formulario, el configurador a escala y el acceso directo por WhatsApp. Teléfono y correo siguen visibles.'
- **"Datos de contacto"**:
  - Campos: TELÉFONO ("+57 300 000 0000") · CORREO ("correo@empresa.com") · WHATSAPP ("573000000000").
  - Ayuda: "WhatsApp: número con indicativo de país y sin espacios (ej. 573178463260)."
- **"Favicon · Icono de pestaña"** — "Es el logo que aparece en la pestaña del navegador. Por defecto usa el logo de 4 paneles del sitio."
  - "Logo de 4 paneles (predeterminado)" — "Usar este logo" · acción "Quitar favicon".
- **"Respaldo"**: "Exportar respaldo" · "Importar respaldo".
  - Confirmación: "Esto reemplazará el contenido actual por el del archivo. ¿Continuar?"
  - Mensajes de validación en 3.10.

#### 5.7.8 Selector de imagen
- "Pega la URL de una imagen" · "Subir archivo" / "Procesando…" · "ELEGIR DE LA GALERÍA:".
- Errores: "El archivo seleccionado no es una imagen." · "La imagen es demasiado pesada ({N} KB). Usa una imagen más pequeña o pega la URL de una imagen alojada en internet." · "No se pudo cargar la imagen."

---

## 6. Observaciones a corregir y decisiones pendientes

### 6.1 Correcciones obligatorias en la versión nueva
1. **Título de portada editable de verdad:** hoy el título grande de Inicio está fijo en el código y `heroTitle` solo cambia la pestaña.
2. **"25 años" desde `experienceYears` en todo el sitio:** hoy está fijo en la cabecera ("25+ AÑOS"), el menú lateral ("25+ AÑOS DE CONFIANZA"), el pie de página, el catálogo ("+25 Años") y los metadatos.
3. **Ortografía:** "PRESCISIÓN MILIMETRICA" → "PRECISIÓN MILIMÉTRICA".
4. **Tuteo en todo el sitio:** "Contanos el espacio…" → "Cuéntanos el espacio…".
5. **Etiquetas engañosas del catálogo:**
   - todas las tarjetas dicen "GALERÍA DE TRABAJOS";
   - el portón "Instalación · Americanos" tiene el botón "Cotizar portón seccional";
   - "Ver galería / Solicitar muestra" lleva a cotizar.

   Proponer textos coherentes.
6. **Textos de ayuda desactualizados del panel:** "Guardar y Publicar", "Aquí llegan las solicitudes de cotización" (WhatsApp) y todo lo de "Publicar / published.json".
7. **Errores siempre en español y entendibles:** hoy, si la API falla, el panel muestra "Cannot read properties of null (reading 'quotes')".
8. **El formulario debe rechazar cotizaciones en el servidor** cuando `quotingEnabled = false`.

### 6.2 Decisiones pendientes (preguntar antes de asumir)
- ¿Se eliminan las divisiones automáticas del vidrio (una hoja cada 150 cm de ancho)?
- ¿El color se llama "Verde Frence" o es otro nombre (por ejemplo, "Verde francés")?
- ¿La galería se muestra al público (por ejemplo, como sección "Trabajos realizados") o solo sirve como banco de imágenes del panel?
- ¿Dónde se despliega? Propuesta: frontend en Vercel; backend + PostgreSQL administrado en Railway, Render o Fly.io.
- ¿Cómo se crea el primer usuario administrador (variable de entorno o cambio obligatorio de contraseña)?

### 6.3 Afirmaciones a validar con la empresa
"Norma NSR" · "Respuesta técnica en menos de 24 horas" · "Instaladores certificados…" · horario "Lunes a Viernes 7:30 AM – 6:00 PM" · "± 1.0 mm".

### 6.4 Migración de datos
Las cotizaciones actuales están en Upstash Redis, en el hash `vya:quotes`. Cada campo es un id y su valor un JSON con:

```json
{ "id": "...", "createdAt": "ISO-8601", "name": "...", "phone": "...", "productType": "...", "dimensions": "...", "message": "..." }
```

Al migrarlas a la tabla `quote`:
- `dimensions` pasa a `dimensions_text`;
- el estado inicial es `NUEVA`;
- los datos estructurados (ancho, alto, vidrio) quedan vacíos porque no existían.
