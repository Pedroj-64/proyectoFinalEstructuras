import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { configurarManejadorErrores } from '../servicios/api.js';

const ContextoAvisos = createContext(null);

/**
 * Avisos emergentes (éxito, error, información) que se muestran en la esquina y desaparecen solos.
 * También registra el manejador de errores de la API para que cada fallo del backend se muestre aquí.
 */
export function ProveedorAvisos({ children }) {
  const [avisos, setAvisos] = useState([]);
  const secuencia = useRef(0);

  const descartar = useCallback((id) => setAvisos((lista) => lista.filter((aviso) => aviso.id !== id)), []);

  const agregar = useCallback(
    (tipo, titulo, mensaje) => {
      const id = ++secuencia.current;
      setAvisos((lista) => [...lista, { id, tipo, titulo, mensaje }]);
      setTimeout(() => descartar(id), tipo === 'error' ? 7000 : 4000);
    },
    [descartar],
  );

  const valor = useMemo(
    () => ({
      avisos,
      descartar,
      exito: (mensaje, titulo = 'Listo') => agregar('exito', titulo, mensaje),
      error: (mensaje, titulo = 'No se pudo completar') => agregar('error', titulo, mensaje),
      info: (mensaje, titulo = 'Información') => agregar('info', titulo, mensaje),
    }),
    [avisos, descartar, agregar],
  );

  useEffect(() => {
    configurarManejadorErrores((error) => agregar('error', error.titulo, error.detalle));
  }, [agregar]);

  return <ContextoAvisos.Provider value={valor}>{children}</ContextoAvisos.Provider>;
}

/** Acceso a los avisos: `avisos.exito('Guardado')`, `avisos.error(...)`, `avisos.info(...)`. */
export function useAvisos() {
  return useContext(ContextoAvisos);
}
