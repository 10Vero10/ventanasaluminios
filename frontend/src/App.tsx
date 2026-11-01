import { Route, Routes } from 'react-router'

// Paso 0: solo comprueba que el proyecto compila y se sirve. Las vistas reales llegan en el paso 5.
function Placeholder() {
  return (
    <main className="flex min-h-dvh items-center justify-center p-6">
      <div className="text-center">
        <h1 className="text-2xl font-semibold">Ventanas y Aluminios SAS</h1>
        <p className="mt-2 text-neutral-700">Sitio en construcción.</p>
      </div>
    </main>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="*" element={<Placeholder />} />
    </Routes>
  )
}
