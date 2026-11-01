# Frontend

Sitio público y panel de administración de Ventanas y Aluminios SAS. React 19 + Vite + TypeScript (`strict`), Tailwind CSS v4, React Router, axios y componentes shadcn/ui (base Radix). Pruebas con Vitest.

## Estructura

```
src/
  auth/            sesión del administrador
  components/
    layout/        cabecera, menú, pie de página y layout del panel
    quote/         configurador de vidrio y plano a escala
    site/          bloques del sitio público (secciones, filtro por línea, llamado a cotizar)
    ui/            componentes base (Button, Field, Dialog, estados) sobre Radix
  lib/             utilidades: reglas del configurador, enlaces de contacto, useLoad, cn()
  pages/           una página por ruta
  routes/          AppRouter y protección de rutas
  site/            SiteProvider y useSite(): configuración pública del sitio
  services/api/    cliente HTTP (httpClient) y un servicio por módulo; las páginas nunca llaman a axios directo
  test/            configuración de Vitest
  types/           tipos que reflejan los DTO del backend
  index.css        Tailwind y tokens de diseño (apple-style-reference)
  main.tsx         punto de entrada
```

Los imports usan el alias `@/` para `src/` (por ejemplo `import { cn } from '@/lib/utils'`).

## Rutas

| Ruta | Página |
|---|---|
| `/` | Inicio |
| `/catalogo?linea=` | Catálogo con filtro por línea y detalle de cada producto |
| `/trabajos?linea=` | Galería de trabajos realizados |
| `/nosotros` | Quiénes somos |
| `/cotizar?producto=` | Configurador y formulario de cotización (con la cotización apagada vuelve al inicio) |
| `/admin/login` | Ingreso al panel |
| `/admin/cambiar-clave` | Cambio de contraseña (obligatorio con la contraseña temporal) |
| `/admin/cotizaciones`, `inicio`, `catalogo`, `vidrio`, `nosotros`, `configuracion` | Pestañas del panel |

El panel (`src/pages/admin/`, `src/components/admin/`) se carga en un archivo aparte: las visitas del sitio público no lo descargan. La sesión vive en una cookie httpOnly; `AuthProvider` la consulta con `/auth/me` y, si el backend responde 401 en el panel, vuelve al login con el aviso "Tu sesión expiró".

## Comandos

Se pueden correr aquí o desde la raíz del repo (los scripts de la raíz delegan en este proyecto).

| Comando | Qué hace |
|---|---|
| `npm install` | Instala las dependencias |
| `npm run dev` | Servidor de desarrollo en http://localhost:5173 |
| `npm run build` | Revisa los tipos y compila a `dist/` |
| `npm run lint` | ESLint |
| `npm test` | Pruebas con Vitest y Testing Library |
| `npm run preview` | Sirve el `dist/` compilado |

## Variables de entorno

Copia `.env.example` como `.env.local` si necesitas cambiar algo.

| Variable | Por defecto | Uso |
|---|---|---|
| `VITE_API_PROXY_TARGET` | `http://localhost:8080` | Backend al que `npm run dev` reenvía `/api` y `/media` |

En producción no hace falta: nginx sirve el sitio y reenvía `/api` y `/media` al backend en el mismo dominio (ver `nginx.conf`).

## Agregar componentes de shadcn/ui

```bash
npx shadcn@latest add button
```

El componente queda copiado en `src/components/ui/` y se adapta a los tokens de `index.css`.
