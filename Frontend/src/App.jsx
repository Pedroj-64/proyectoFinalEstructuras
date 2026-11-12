import { Suspense } from 'react';
import { Cargando } from './componentes/Estados.jsx';
import { Isla } from './componentes/Isla.jsx';
import { PilaAvisos } from './componentes/PilaAvisos.jsx';
import { useNavegacion } from './navegacion/Navegacion.jsx';

/**
 * Estructura de la aplicación: isla de navegación flotante, la pantalla activa y los avisos.
 * `key={pagina.id}` reinicia el estado de cada pantalla al cambiar de una a otra.
 * Mientras llega el código de una pantalla nueva (`pendiente`) la actual se atenúa un instante.
 */
export default function App() {
  const { pagina, parametros, pendiente } = useNavegacion();
  const { Componente } = pagina;

  return (
    <>
      <Isla />
      <main style={{ opacity: pendiente ? 0.55 : 1, transition: 'opacity 0.5s var(--muelle)' }}>
        <Suspense
          fallback={
            <div className="pagina">
              <Cargando bloques={4} />
            </div>
          }
        >
          <Componente key={pagina.id} parametros={parametros} />
        </Suspense>
      </main>
      <footer className="pie">Casa Quindío · Sistema de gestión de aparta-hotel · Proyecto de Estructuras de Datos</footer>
      <PilaAvisos />
    </>
  );
}
