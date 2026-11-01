# Frontend

Sitio público y panel de administración de Ventanas y Aluminios SAS. React 19 + Vite + TypeScript (`strict`), Tailwind CSS v4, React Router, axios y componentes shadcn/ui (base Radix). Pruebas con Vitest.

## Estructura

```
src/
  auth/            sesión del administrador (paso 4)
  components/
    layout/        cabecera, menú, pie de página y layout del panel
    ui/            componentes de shadcn/ui (se agregan con `npx shadcn add <nombre>`)
  lib/             utilidades (cn() combina clases de Tailwind sin conflictos)
  pages/           una página por ruta
  routes/          AppRouter y protección de rutas
  services/api/    cliente HTTP (httpClient) y un servicio por módulo; las páginas nunca llaman a axios directo
  test/            configuración de Vitest
  types/           tipos que reflejan los DTO del backend
  index.css        Tailwind y tokens de diseño (apple-style-reference)
  main.tsx         punto de entrada
```

Los imports usan el alias `@/` para `src/` (por ejemplo `import { cn } from '@/lib/utils'`).

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
