/** Formateador de pesos colombianos sin decimales. */
const formateador = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });

/** Formatea un valor en pesos colombianos; devuelve un guion si no hay valor. */
export function formatearDinero(valor) {
  return valor == null ? '—' : formateador.format(valor);
}

/** Formateador compacto (`$ 9,9 M`) para etiquetas donde no cabe el monto completo, como las gráficas. */
const formateadorCorto = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', notation: 'compact', maximumFractionDigits: 1 });

/** Formatea un monto en forma abreviada; devuelve un guion si no hay valor. */
export function formatearDineroCorto(valor) {
  return valor == null ? '—' : formateadorCorto.format(valor);
}
