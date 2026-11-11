import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { administracionApi } from '../servicios/api.js';
import { fechaIso } from '../utilidades/fechas.js';

const ContextoReloj = createContext(null);

/**
 * Reloj del servidor: el backend puede simular otra fecha (para probar vencimientos y llegadas),
 * así que las pantallas deben usar `hoy` de aquí y no la fecha del navegador.
 */
export function ProveedorReloj({ children }) {
  const [estado, setEstado] = useState(null);

  const refrescar = useCallback(() => {
    administracionApi
      .reloj()
      .then(setEstado)
      .catch(() => {});
  }, []);

  useEffect(() => {
    refrescar();
    const temporizador = setInterval(refrescar, 60_000);
    return () => clearInterval(temporizador);
  }, [refrescar]);

  const valor = useMemo(
    () => ({
      /** Estado del reloj: `{ ahora, simulado }` o `null` mientras carga. */
      estado,
      /** Fecha de hoy según el servidor, en formato ISO. */
      hoy: estado?.ahora ? estado.ahora.slice(0, 10) : fechaIso(new Date()),
      refrescar,
      fijar: setEstado,
    }),
    [estado, refrescar],
  );

  return <ContextoReloj.Provider value={valor}>{children}</ContextoReloj.Provider>;
}

/** Acceso al reloj del servidor desde cualquier componente. */
export function useReloj() {
  return useContext(ContextoReloj);
}
