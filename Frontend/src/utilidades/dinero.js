/** Formateador de pesos colombianos sin decimales. */
const formateador = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });

/** Formatea un valor en pesos colombianos; devuelve un guion si no hay valor. */
export function formatearDinero(valor) {
  return valor == null ? '—' : formateador.format(valor);
}
