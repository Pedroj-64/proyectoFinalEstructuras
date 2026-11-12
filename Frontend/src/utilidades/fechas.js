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

/** Crea un `Date` local a partir de una fecha ISO (evita el desfase de zona horaria de `new Date(iso)`). */
function aFecha(iso) {
  const [anio, mes, dia] = iso.slice(0, 10).split('-').map(Number);
  return new Date(anio, mes - 1, dia);
}

/** Formatea una fecha ISO como `dd/mm/aaaa`; devuelve un guion si no hay valor. */
export function formatearFecha(iso) {
  if (!iso) return '—';
  return aFecha(iso).toLocaleDateString('es-CO', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

/** Formatea una fecha ISO de forma corta: `16 nov`. */
export function fechaCorta(iso) {
  if (!iso) return '—';
  return aFecha(iso).toLocaleDateString('es-CO', { day: 'numeric', month: 'short' }).replace('.', '');
}

/** Formatea un rango de estancia: `16 nov → 19 nov`. */
export function formatearRango(ingreso, salida) {
  return `${fechaCorta(ingreso)} → ${fechaCorta(salida)}`;
}

/** Formatea una fecha y hora ISO para mostrarla; devuelve un guion si no hay valor. */
export function formatearFechaHora(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
}

/** Formatea solo la hora (`HH:mm`) de una fecha-hora ISO o de un texto `HH:mm:ss`. */
export function formatearHora(valor) {
  if (!valor) return '—';
  const texto = valor.includes('T') ? valor.split('T')[1] : valor;
  return texto.slice(0, 5);
}

/** Mes ISO `aaaa-mm` de una fecha ISO. */
export function mesDe(iso) {
  return iso.slice(0, 7);
}

/** Suma (o resta) meses a un mes ISO `aaaa-mm`. */
export function sumarMeses(mes, cantidad) {
  const [anio, numero] = mes.split('-').map(Number);
  return fechaIso(new Date(anio, numero - 1 + cantidad, 1)).slice(0, 7);
}

/** Nombre del mes con año: `noviembre de 2026`. */
export function nombreMes(mes) {
  const [anio, numero] = mes.split('-').map(Number);
  return new Date(anio, numero - 1, 1).toLocaleDateString('es-CO', { month: 'long', year: 'numeric' });
}

/** Saludo según la hora del día (0-23). */
export function saludo(hora) {
  if (hora < 12) return 'Buenos días';
  if (hora < 19) return 'Buenas tardes';
  return 'Buenas noches';
}

/** Texto relativo hasta una fecha-hora futura: `3 h 20 min` o `vencida`. */
export function tiempoRestante(isoFechaHora, ahoraIso) {
  const resta = new Date(isoFechaHora) - new Date(ahoraIso);
  if (resta <= 0) return 'vencida';
  const minutos = Math.floor(resta / 60000);
  const horas = Math.floor(minutos / 60);
  return horas > 0 ? `${horas} h ${minutos % 60} min` : `${minutos} min`;
}
