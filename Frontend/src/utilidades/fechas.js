/** Utilidades de fechas ISO (`aaaa-mm-dd`) y de formato para mostrar en pantalla. */

/** Convierte un `Date` a texto ISO `aaaa-mm-dd` usando la zona horaria local. */
export function fechaIso(fecha) {
  const local = new Date(fecha.getTime() - fecha.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 10);
}

/** Suma (o resta, si es negativo) días a una fecha ISO. */
export function sumarDias(iso, dias) {
  const [anio, mes, dia] = iso.split('-').map(Number);
  return fechaIso(new Date(anio, mes - 1, dia + dias));
}

/** Fecha de hoy del navegador en formato ISO. */
export function hoy() {
  return fechaIso(new Date());
}

/** Formatea una fecha ISO como `dd/mm/aaaa`; devuelve un guion si no hay valor. */
export function formatearFecha(iso) {
  if (!iso) return '—';
  const [anio, mes, dia] = iso.slice(0, 10).split('-').map(Number);
  return new Date(anio, mes - 1, dia).toLocaleDateString('es-CO', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

/** Formatea una fecha y hora ISO para mostrarla; devuelve un guion si no hay valor. */
export function formatearFechaHora(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
}
