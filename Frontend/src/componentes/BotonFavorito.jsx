import { Heart } from '@phosphor-icons/react';
import { startTransition, useOptimistic } from 'react';
import { useAvisos } from '../contexto/Avisos.jsx';
import { huespedesApi } from '../servicios/api.js';

/**
 * Corazón para marcar un apartamento como favorito.
 * Usa `useOptimistic` (React 19): el corazón cambia al instante y, si la API falla, vuelve solo
 * a su valor real cuando termina la transición.
 *
 * Props: `documento` del huésped, `codigo` del apartamento, `esFavorito` (valor confirmado por el
 * servidor) y `alCambiar` para que el padre recargue la lista de favoritos.
 */
export function BotonFavorito({ documento, codigo, esFavorito, alCambiar }) {
  const avisos = useAvisos();
  const [marcado, marcarOptimista] = useOptimistic(esFavorito);

  function alternar() {
    startTransition(async () => {
      marcarOptimista(!esFavorito);
      try {
        if (esFavorito) await huespedesApi.quitarFavorito(documento, codigo);
        else await huespedesApi.agregarFavorito(documento, codigo);
        avisos.info(esFavorito ? 'Quitado de tus favoritos' : 'Guardado en tus favoritos', codigo);
        alCambiar();
      } catch {
        // el cliente de la API ya avisó; el estado optimista se revierte solo
      }
    });
  }

  return (
    <button type="button" className={`corazon ${marcado ? 'corazon--activo' : ''}`} aria-pressed={marcado} aria-label={marcado ? 'Quitar de favoritos' : 'Guardar en favoritos'} onClick={alternar}>
      <Heart weight={marcado ? 'fill' : 'light'} />
    </button>
  );
}
