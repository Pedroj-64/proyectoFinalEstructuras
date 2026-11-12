import { createContext, useCallback, useContext, useMemo, useRef, useState, useTransition } from 'react';
import { useSesion } from '../contexto/Sesion.jsx';
import { Pila } from '../utilidades/pila.js';
import { paginasDelRol } from './paginas.js';

const ContextoNavegacion = createContext(null);

/**
 * Navegación entre pantallas sin librería de rutas.
 *
 * - `irRaiz(id)`: salta a una pantalla del menú y vacía el historial.
 * - `ir(id, parametros)`: entra a una pantalla desde otra y guarda la actual en una `Pila` (LIFO),
 *   que es justo lo que alimenta el botón "Atrás".
 * - `atras()`: desapila y vuelve a la pantalla anterior.
 *
 * Los cambios van dentro de `startTransition`: React mantiene la pantalla actual visible mientras
 * se descarga el código de la siguiente (`pendiente` sirve para atenuarla) en vez de mostrar un hueco.
 */
export function ProveedorNavegacion({ children }) {
  const { rol } = useSesion();
  const [actual, setActual] = useState({ id: null, parametros: {} });
  const [puedeVolver, setPuedeVolver] = useState(false);
  const [pendiente, iniciarTransicion] = useTransition();
  const historial = useRef(new Pila());

  const permitidas = useMemo(() => paginasDelRol(rol), [rol]);
  // Si la pantalla guardada no existe para este rol (p. ej. al cambiar de rol), se usa la primera.
  const pagina = permitidas.find((candidata) => candidata.id === actual.id) ?? permitidas[0];

  const irRaiz = useCallback((id) => {
    historial.current.vaciar();
    setPuedeVolver(false);
    iniciarTransicion(() => setActual({ id, parametros: {} }));
  }, []);

  const ir = useCallback(
    (id, parametros = {}) => {
      historial.current.apilar({ id: pagina.id, parametros: actual.parametros });
      setPuedeVolver(true);
      iniciarTransicion(() => setActual({ id, parametros }));
    },
    [pagina.id, actual.parametros],
  );

  const atras = useCallback(() => {
    if (historial.current.estaVacia()) return;
    const anterior = historial.current.desapilar();
    setPuedeVolver(!historial.current.estaVacia());
    iniciarTransicion(() => setActual(anterior));
  }, []);

  const valor = useMemo(
    () => ({ pagina, parametros: actual.parametros, permitidas, puedeVolver, pendiente, ir, irRaiz, atras }),
    [pagina, actual.parametros, permitidas, puedeVolver, pendiente, ir, irRaiz, atras],
  );

  return <ContextoNavegacion.Provider value={valor}>{children}</ContextoNavegacion.Provider>;
}

/** Acceso a la navegación desde cualquier componente. */
export function useNavegacion() {
  return useContext(ContextoNavegacion);
}
