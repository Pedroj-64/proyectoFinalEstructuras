/**
 * Cliente de la API REST del backend (Spring Boot). Todas las funciones devuelven promesas con el JSON
 * de la respuesta. Si el servidor responde con error, se lanza un `ErrorApi` con el `ProblemDetail`.
 *
 * La función `configurarManejadorErrores` permite que la aplicación muestre cada error como aviso.
 */
const URL_BASE = '/api';

/** Error de una llamada a la API: conserva el estado HTTP y el título/detalle que envía el backend. */
export class ErrorApi extends Error {
  constructor(estado, titulo, detalle) {
    super(detalle || titulo);
    this.estado = estado;
    this.titulo = titulo;
    this.detalle = detalle;
  }
}

/** Función que recibe cada `ErrorApi` para avisarle al usuario (se registra desde la aplicación). */
let manejadorErrores = () => {};

/** Registra la función que se llama con cada error de la API. */
export function configurarManejadorErrores(funcion) {
  manejadorErrores = funcion;
}

/** Arma la cadena de consulta `?a=1&b=2` omitiendo los valores vacíos. */
function consulta(parametros) {
  const busqueda = new URLSearchParams();
  for (const [nombre, valor] of Object.entries(parametros ?? {})) {
    if (valor !== undefined && valor !== null && valor !== '') busqueda.set(nombre, valor);
  }
  const texto = busqueda.toString();
  return texto ? `?${texto}` : '';
}

/** Ejecuta la petición y traduce los errores HTTP a `ErrorApi` (avisando al usuario una sola vez). */
async function pedir(metodo, ruta, { cuerpo, parametros, formulario } = {}) {
  const opciones = { method: metodo, headers: {} };
  if (formulario) {
    opciones.body = formulario;
  } else if (cuerpo !== undefined) {
    opciones.headers['Content-Type'] = 'application/json';
    opciones.body = JSON.stringify(cuerpo);
  }
  let respuesta;
  try {
    respuesta = await fetch(`${URL_BASE}${ruta}${consulta(parametros)}`, opciones);
  } catch {
    const error = new ErrorApi(0, 'Sin conexión', 'No hay conexión con el servidor. ¿Está corriendo el backend en el puerto 8080?');
    manejadorErrores(error);
    throw error;
  }
  if (!respuesta.ok) {
    let problema = null;
    try {
      problema = await respuesta.json();
    } catch {
      // la respuesta de error no traía JSON
    }
    const error = new ErrorApi(respuesta.status, problema?.title ?? `Error ${respuesta.status}`, problema?.detail ?? respuesta.statusText);
    manejadorErrores(error);
    throw error;
  }
  if (respuesta.status === 204) return null;
  const texto = await respuesta.text();
  return texto ? JSON.parse(texto) : null;
}

const obtener = (ruta, parametros) => pedir('GET', ruta, { parametros });
const enviar = (ruta, cuerpo = {}) => pedir('POST', ruta, { cuerpo });
const reemplazar = (ruta, cuerpo) => pedir('PUT', ruta, { cuerpo });
const modificar = (ruta, cuerpo) => pedir('PATCH', ruta, { cuerpo });
const eliminar = (ruta) => pedir('DELETE', ruta);

/** Apartamentos: catálogo, tarifas, estado y disponibilidad. */
export const apartamentosApi = {
  listar: () => obtener('/apartamentos'),
  buscar: (codigo) => obtener(`/apartamentos/${codigo}`),
  crear: (solicitud) => enviar('/apartamentos', solicitud),
  actualizar: (codigo, solicitud) => reemplazar(`/apartamentos/${codigo}`, solicitud),
  actualizarTarifas: (codigo, tarifas) => reemplazar(`/apartamentos/${codigo}/tarifas`, tarifas),
  cambiarEstado: (codigo, estado, motivo) => modificar(`/apartamentos/${codigo}/estado`, { estado, motivo }),
  eliminar: (codigo) => eliminar(`/apartamentos/${codigo}`),
  reservasDe: (codigo) => obtener(`/apartamentos/${codigo}/reservas`),
  /** Apartamentos libres para un rango de fechas y cantidad de huéspedes (`ordenar`: PRECIO, CAPACIDAD o HABITACIONES). */
  disponibilidad: (ingreso, salida, huespedes, ordenar) => obtener('/disponibilidad', { ingreso, salida, huespedes, ordenar }),
};

/** Reservas: ciclo de vida, cotización, grupos y folio. */
export const reservasApi = {
  listar: (filtros = {}) => obtener('/reservas', { estado: filtros.estado, canal: filtros.canal }),
  buscar: (id) => obtener(`/reservas/${id}`),
  crear: (solicitud) => enviar('/reservas', solicitud),
  planGrupal: (ingreso, salida, tamanoGrupo) => obtener('/reservas/plan-grupal', { ingreso, salida, tamanoGrupo }),
  crearGrupo: (solicitud) => enviar('/reservas/grupo', solicitud),
  cotizar: (codigoApartamento, ingreso, salida, ocupantes) => enviar('/cotizaciones', { codigoApartamento, ingreso, salida, ocupantes }),
  vistaPreviaReembolso: (id) => obtener(`/reservas/${id}/vista-previa-reembolso`),
  cancelar: (id) => enviar(`/reservas/${id}/cancelar`),
  registrarIngreso: (id) => enviar(`/reservas/${id}/ingreso`),
  registrarSalida: (id) => enviar(`/reservas/${id}/salida`),
  marcarNoPresentado: (id) => enviar(`/reservas/${id}/no-presentado`),
  vencimientosPendientes: () => obtener('/reservas/vencimientos-pendientes'),
  folio: (id) => obtener(`/reservas/${id}/folio`),
  agregarCargo: (id, movimiento) => enviar(`/reservas/${id}/folio/cargos`, movimiento),
  agregarPago: (id, movimiento) => enviar(`/reservas/${id}/folio/pagos`, movimiento),
  deshacerUltimo: (id) => enviar(`/reservas/${id}/folio/deshacer`),
};

/** Huéspedes: registro, favoritos, historial y reservas propias. */
export const huespedesApi = {
  listar: () => obtener('/huespedes'),
  buscar: (documento) => obtener(`/huespedes/${documento}`),
  registrar: (solicitud) => enviar('/huespedes', solicitud),
  actualizar: (documento, solicitud) => reemplazar(`/huespedes/${documento}`, solicitud),
  eliminar: (documento) => eliminar(`/huespedes/${documento}`),
  favoritos: (documento) => obtener(`/huespedes/${documento}/favoritos`),
  agregarFavorito: (documento, codigoApartamento) => enviar(`/huespedes/${documento}/favoritos`, { codigoApartamento }),
  quitarFavorito: (documento, codigoApartamento) => eliminar(`/huespedes/${documento}/favoritos/${codigoApartamento}`),
  historial: (documento) => obtener(`/huespedes/${documento}/historial`),
  reservas: (documento) => obtener(`/huespedes/${documento}/reservas`),
};

/** Operación diaria: preparación de apartamentos, mapa del edificio, canal externo y turnos. */
export const operacionesApi = {
  colaPreparacion: () => obtener('/preparacion/cola'),
  enPreparacion: () => obtener('/preparacion/en-curso'),
  iniciarSiguientePreparacion: () => enviar('/preparacion/iniciar'),
  finalizarPreparacion: (codigo) => enviar(`/preparacion/${codigo}/terminar`),
  rutaPreparacion: () => obtener('/preparacion/ruta'),

  mapaEdificio: () => obtener('/mapa-edificio'),
  establecerPasilloAbierto: (desde, hasta, abierto) => modificar('/mapa-edificio/pasillos', { desde, hasta, abierto }),
  caminoMasCorto: (desde, hasta) => obtener('/mapa-edificio/ruta', { desde, hasta }),
  analisis: (inicio) => obtener('/mapa-edificio/analisis', { inicio }),
  impactoCierre: (desde, hasta) => obtener('/mapa-edificio/impacto-cierre', { desde, hasta }),

  colaExterna: () => obtener('/canal-externo/solicitudes'),
  enviarSolicitudExterna: (solicitud) => enviar('/canal-externo/solicitudes', solicitud),
  procesarSiguienteExterna: () => enviar('/canal-externo/procesar-siguiente'),
  procesarTodasExternas: () => enviar('/canal-externo/procesar'),
  resultadosExternos: () => obtener('/canal-externo/resultados'),
  conflictosCanal: () => obtener('/canal-externo/conflictos'),

  turnoActual: () => obtener('/turnos/actual'),
  turnosProximos: (cantidad = 6) => obtener('/turnos/proximos', { cantidad }),
};

/** Administración: empleados, temporadas, reportes, calendario, carga de datos y reloj simulado. */
export const administracionApi = {
  empleados: () => obtener('/empleados'),
  crearEmpleado: (solicitud) => enviar('/empleados', solicitud),
  actualizarEmpleado: (id, solicitud) => reemplazar(`/empleados/${id}`, solicitud),
  eliminarEmpleado: (id) => eliminar(`/empleados/${id}`),

  temporadas: () => obtener('/temporadas'),
  reemplazarTemporadas: (periodos) => reemplazar('/temporadas', periodos),

  reporte: (desde, hasta) => obtener('/reportes/resumen', { desde, hasta }),
  calendario: (mes) => obtener('/calendario', { mes }),

  cargarEscenarioPorDefecto: () => enviar('/sistema/escenario/por-defecto'),
  cargarEscenario: (archivo) => {
    const formulario = new FormData();
    formulario.append('archivo', archivo);
    return pedir('POST', '/sistema/escenario/cargar', { formulario });
  },
  urlPlantillaEscenario: () => `${URL_BASE}/sistema/escenario/plantilla`,

  reloj: () => obtener('/sistema/reloj'),
  viajarA: (fechaHora) => reemplazar('/sistema/reloj', { fechaHora }),
  reiniciarReloj: () => eliminar('/sistema/reloj'),
};
