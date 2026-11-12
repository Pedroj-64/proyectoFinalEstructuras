/**
 * Tonos de color y reglas de estado compartidas por las pantallas.
 * Los tonos son los nombres de las variantes de `Insignia` (`salvia`, `azul`, `ambar`, `rojo`, `neutro`).
 */

/** Tono de cada estado de reserva. */
export const TONO_RESERVA = {
  PENDIENTE: 'ambar',
  CONFIRMADA: 'azul',
  EN_CURSO: 'salvia',
  FINALIZADA: 'neutro',
  CANCELADA: 'rojo',
  NO_PRESENTADO: 'rojo',
};

/** Tono de cada estado operativo de un apartamento. */
export const TONO_APARTAMENTO = {
  PREPARADO: 'salvia',
  OCUPADO: 'azul',
  PENDIENTE_PREPARACION: 'ambar',
  EN_PREPARACION: 'ambar',
  FUERA_DE_SERVICIO: 'rojo',
};

/** Estados a los que puede pasar un apartamento (replica `EstadoApartamento.puedePasarA` del backend). */
export const TRANSICIONES_APARTAMENTO = {
  PREPARADO: ['OCUPADO', 'FUERA_DE_SERVICIO'],
  OCUPADO: ['PENDIENTE_PREPARACION'],
  PENDIENTE_PREPARACION: ['EN_PREPARACION', 'FUERA_DE_SERVICIO'],
  EN_PREPARACION: ['PREPARADO', 'FUERA_DE_SERVICIO'],
  FUERA_DE_SERVICIO: ['PENDIENTE_PREPARACION', 'PREPARADO'],
};

/** Nombre legible de cada estado de apartamento. */
export const ETIQUETA_APARTAMENTO = {
  PREPARADO: 'Preparado',
  OCUPADO: 'Ocupado',
  PENDIENTE_PREPARACION: 'Pendiente de preparación',
  EN_PREPARACION: 'En preparación',
  FUERA_DE_SERVICIO: 'Fuera de servicio',
};

/** Tono de cada temporada tarifaria. */
export const TONO_TEMPORADA = { BAJA: 'neutro', MEDIA: 'ambar', ALTA: 'rojo' };

/** Canales por los que llega una reserva. */
export const CANALES = ['PORTAL', 'DIRECTO', 'EXTERNO'];

/** Estados de reserva en el orden del ciclo de vida. */
export const ESTADOS_RESERVA = ['PENDIENTE', 'CONFIRMADA', 'EN_CURSO', 'FINALIZADA', 'CANCELADA', 'NO_PRESENTADO'];

/** Iniciales de un nombre (máximo dos letras) para los avatares. */
export function iniciales(nombre) {
  return (nombre ?? '?')
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((parte) => parte[0].toUpperCase())
    .join('');
}
