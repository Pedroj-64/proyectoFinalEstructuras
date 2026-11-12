import { useCallback, useEffect, useRef, useState } from 'react';

/**
 * Carga datos asíncronos (normalmente una llamada a la API) y los mantiene en estado.
 *
 * - `funcion` devuelve una promesa; se vuelve a ejecutar cuando cambia algo de `dependencias`
 *   o cuando se llama a `recargar()`.
 * - Mientras llega la nueva respuesta se conservan los datos anteriores (`actualizando`), así la
 *   pantalla no parpadea al filtrar o recargar.
 * - Los errores ya los muestra el cliente de la API como aviso; aquí solo se guardan en `error`.
 *
 * @returns `{ datos, cargando, actualizando, error, recargar, cambiar }`
 *   `cargando` es verdadero solo la primera vez (aún no hay datos); `cambiar` reemplaza los datos
 *   localmente (útil para actualizaciones optimistas).
 */
export function useConsulta(funcion, dependencias = []) {
  const [version, setVersion] = useState(0);
  const [resultado, setResultado] = useState({ clave: null, datos: undefined, error: null });
  const ultimaFuncion = useRef(funcion);

  useEffect(() => {
    ultimaFuncion.current = funcion;
  });

  const clave = `${version}:${JSON.stringify(dependencias)}`;

  useEffect(() => {
    let vigente = true;
    ultimaFuncion
      .current()
      .then((datos) => vigente && setResultado({ clave, datos, error: null }))
      .catch((error) => vigente && setResultado((previo) => ({ clave, datos: previo.datos, error })));
    return () => {
      vigente = false;
    };
  }, [clave]);

  const recargar = useCallback(() => setVersion((valor) => valor + 1), []);
  const cambiar = useCallback((datos) => setResultado((previo) => ({ ...previo, datos })), []);

  return {
    datos: resultado.datos,
    cargando: resultado.datos === undefined && !resultado.error,
    actualizando: resultado.clave !== clave,
    error: resultado.error,
    recargar,
    cambiar,
  };
}
