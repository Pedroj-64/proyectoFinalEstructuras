import { ArrowCounterClockwise, Clock, DownloadSimple, PencilSimple, Plus, Trash, UploadSimple } from '@phosphor-icons/react';
import { useActionState, useRef, useState } from 'react';
import { Bisel } from '../componentes/Bisel.jsx';
import { Boton } from '../componentes/Boton.jsx';
import { Encabezado } from '../componentes/Encabezado.jsx';
import { Cargando } from '../componentes/Estados.jsx';
import { Insignia } from '../componentes/Insignia.jsx';
import { Modal } from '../componentes/Modal.jsx';
import { Revelar } from '../componentes/Revelar.jsx';
import { useAvisos } from '../contexto/Avisos.jsx';
import { useReloj } from '../contexto/Reloj.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { administracionApi, operacionesApi } from '../servicios/api.js';
import { iniciales } from '../utilidades/estados.js';
import { formatearFechaHora } from '../utilidades/fechas.js';

const ROLES_EMPLEADO = [
  ['RECEPCIONISTA', 'Recepcionista'],
  ['ASEO', 'Personal de preparación'],
  ['ADMINISTRADOR', 'Administrador'],
];

/** Formulario de alta o edición de un empleado. */
function FormularioEmpleado({ empleado, alTerminar }) {
  const avisos = useAvisos();
  const editando = Boolean(empleado);
  const [, accion, enviando] = useActionState(async (_previo, formulario) => {
    const solicitud = {
      id: formulario.get('id').trim(),
      nombreCompleto: formulario.get('nombre').trim(),
      rol: formulario.get('rol'),
      correo: formulario.get('correo') || null,
      telefono: formulario.get('telefono') || null,
    };
    try {
      if (editando) await administracionApi.actualizarEmpleado(empleado.id, solicitud);
      else await administracionApi.crearEmpleado(solicitud);
      avisos.exito(editando ? 'Empleado actualizado' : 'Empleado creado');
      alTerminar();
    } catch {
      // aviso ya mostrado
    }
    return null;
  }, null);

  return (
    <form action={accion} className="formulario formulario--ancho">
      <label className="campo">
        <span>Código</span>
        <input name="id" required defaultValue={empleado?.id} readOnly={editando} placeholder="E-007" />
      </label>
      <label className="campo">
        <span>Nombre completo</span>
        <input name="nombre" required defaultValue={empleado?.nombreCompleto} />
      </label>
      <label className="campo">
        <span>Rol</span>
        <select name="rol" defaultValue={empleado?.rol ?? 'RECEPCIONISTA'}>
          {ROLES_EMPLEADO.map(([valor, etiqueta]) => (
            <option key={valor} value={valor}>
              {etiqueta}
            </option>
          ))}
        </select>
      </label>
      <label className="campo">
        <span>Teléfono</span>
        <input name="telefono" defaultValue={empleado?.telefono ?? ''} />
      </label>
      <label className="campo" style={{ gridColumn: '1 / -1' }}>
        <span>Correo</span>
        <input name="correo" type="email" defaultValue={empleado?.correo ?? ''} />
      </label>
      <div className="formulario__pie">
        <Boton variante="suave" onClick={alTerminar}>
          Cancelar
        </Boton>
        <Boton type="submit" variante="salvia" disabled={enviando}>
          {editando ? 'Guardar' : 'Crear empleado'}
        </Boton>
      </div>
    </form>
  );
}

/** Pestaña de personal: empleados y próximos turnos de recepción. */
function Personal() {
  const avisos = useAvisos();
  const [edicion, setEdicion] = useState(null);
  const { datos, cargando, recargar } = useConsulta(async () => {
    const [empleados, turnos] = await Promise.all([administracionApi.empleados(), operacionesApi.turnosProximos(6).catch(() => [])]);
    return { empleados, turnos };
  });

  async function eliminar(empleado) {
    if (!window.confirm(`¿Eliminar a ${empleado.nombreCompleto}?`)) return;
    try {
      await administracionApi.eliminarEmpleado(empleado.id);
      avisos.exito('Empleado eliminado');
      recargar();
    } catch {
      // aviso ya mostrado
    }
  }

  if (cargando) return <Cargando bloques={3} />;
  return (
    <div className="bento">
      <div className="c8">
        <Bisel>
          <div className="pila">
            <div className="fila separado">
              <h2 className="titulo-seccion">
                Equipo <em>de trabajo</em>
              </h2>
              <Boton pequeno isla={Plus} onClick={() => setEdicion({})}>
                Nuevo
              </Boton>
            </div>
            <ul className="lista">
              {datos.empleados.map((empleado) => (
                <li key={empleado.id} className="fila-lista fila-lista--fija">
                  <span className="avatar">{iniciales(empleado.nombreCompleto)}</span>
                  <div className="celda-doble" style={{ flex: 1 }}>
                    <strong>{empleado.nombreCompleto}</strong>
                    <small>
                      {empleado.id} · {empleado.correo ?? 'sin correo'}
                    </small>
                  </div>
                  <Insignia tono={empleado.rol === 'ADMINISTRADOR' ? 'azul' : empleado.rol === 'RECEPCIONISTA' ? 'salvia' : 'ambar'}>{empleado.etiquetaRol}</Insignia>
                  <Boton variante="fantasma" pequeno soloIcono aria-label="Editar" onClick={() => setEdicion(empleado)}>
                    <PencilSimple weight="light" />
                  </Boton>
                  <Boton variante="fantasma" pequeno soloIcono aria-label="Eliminar" onClick={() => eliminar(empleado)}>
                    <Trash weight="light" />
                  </Boton>
                </li>
              ))}
            </ul>
          </div>
        </Bisel>
      </div>
      <div className="c4">
        <Bisel>
          <div className="pila">
            <h2 className="titulo-tarjeta">Próximos turnos</h2>
            <ul className="lista">
              {datos.turnos.map((turno) => (
                <li key={turno.inicio} className="fila-lista fila-lista--fija">
                  <div className="celda-doble">
                    <strong>{turno.nombreRecepcionista}</strong>
                    <small>{formatearFechaHora(turno.inicio)}</small>
                  </div>
                  <Insignia>{turno.etiquetaFranja}</Insignia>
                </li>
              ))}
            </ul>
          </div>
        </Bisel>
      </div>
      <Modal abierto={Boolean(edicion)} alCerrar={() => setEdicion(null)} eyebrow="Personal" titulo={edicion?.id ? 'Editar empleado' : 'Nuevo empleado'} ancho={640}>
        {edicion && (
          <FormularioEmpleado
            empleado={edicion.id ? edicion : null}
            alTerminar={() => {
              setEdicion(null);
              recargar();
            }}
          />
        )}
      </Modal>
    </div>
  );
}

/** Editor de los periodos de temporada. Parte de una copia local y envía la lista completa al guardar. */
function EditorTemporadas({ inicial, alGuardar }) {
  const avisos = useAvisos();
  const [periodos, setPeriodos] = useState(inicial);
  const [guardando, setGuardando] = useState(false);

  const cambiar = (indice, campo, valor) => setPeriodos((lista) => lista.map((p, i) => (i === indice ? { ...p, [campo]: valor } : p)));

  async function guardar() {
    setGuardando(true);
    try {
      await administracionApi.reemplazarTemporadas(periodos.map(({ temporada, inicio, fin }) => ({ temporada, inicio, fin })));
      avisos.exito('Temporadas actualizadas. Las nuevas cotizaciones usarán estas fechas.');
      alGuardar();
    } catch {
      // aviso ya mostrado
    } finally {
      setGuardando(false);
    }
  }

  return (
    <Bisel>
      <div className="pila" style={{ gap: '1.25rem' }}>
        <div className="fila separado">
          <h2 className="titulo-seccion">
            Calendario de <em>temporadas</em>
          </h2>
          <Boton variante="suave" pequeno icono={Plus} onClick={() => setPeriodos((l) => [...l, { temporada: 'ALTA', inicio: '', fin: '' }])}>
            Añadir periodo
          </Boton>
        </div>
        <p className="tenue">La temporada de cada noche decide la tarifa por persona. Las fechas que no estén cubiertas se cobran como temporada baja.</p>
        {periodos.map((periodo, indice) => (
          <div key={indice} className="formulario">
            <label className="campo">
              <span>Temporada</span>
              <select value={periodo.temporada} onChange={(e) => cambiar(indice, 'temporada', e.target.value)}>
                <option value="BAJA">Baja</option>
                <option value="MEDIA">Media</option>
                <option value="ALTA">Alta</option>
              </select>
            </label>
            <label className="campo">
              <span>Desde</span>
              <input type="date" value={periodo.inicio} onChange={(e) => cambiar(indice, 'inicio', e.target.value)} />
            </label>
            <label className="campo">
              <span>Hasta</span>
              <input type="date" value={periodo.fin} onChange={(e) => cambiar(indice, 'fin', e.target.value)} />
            </label>
            <Boton variante="fantasma" icono={Trash} onClick={() => setPeriodos((l) => l.filter((_, i) => i !== indice))}>
              Quitar
            </Boton>
          </div>
        ))}
        <div className="formulario__pie" style={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Boton variante="salvia" disabled={guardando || periodos.some((p) => !p.inicio || !p.fin)} onClick={guardar}>
            Guardar temporadas
          </Boton>
        </div>
      </div>
    </Bisel>
  );
}

function Temporadas() {
  const { datos, cargando, recargar } = useConsulta(administracionApi.temporadas);
  if (cargando) return <Cargando bloques={2} />;
  return <EditorTemporadas inicial={datos} alGuardar={recargar} />;
}

/** Pestaña de sistema: reloj simulado y carga de escenarios de datos. */
function Sistema() {
  const avisos = useAvisos();
  const { estado, fijar } = useReloj();
  const [fecha, setFecha] = useState('');
  const archivo = useRef(null);

  async function viajar() {
    try {
      fijar(await administracionApi.viajarA(fecha));
      avisos.exito('El reloj del sistema se movió. Las reservas pendientes se evalúan con la nueva hora.', 'Viaje en el tiempo');
    } catch {
      // aviso ya mostrado
    }
  }

  async function reiniciar() {
    try {
      fijar(await administracionApi.reiniciarReloj());
      avisos.exito('El reloj volvió a la hora real.');
    } catch {
      // aviso ya mostrado
    }
  }

  async function cargarPorDefecto() {
    if (!window.confirm('Esto reemplaza todos los datos actuales por el escenario de demostración. ¿Continuar?')) return;
    try {
      const resumen = await administracionApi.cargarEscenarioPorDefecto();
      avisos.exito(`${resumen.apartamentos} apartamentos y ${resumen.reservas} reservas cargados.`, 'Escenario cargado');
    } catch {
      // aviso ya mostrado
    }
  }

  async function subir(evento) {
    const elegido = evento.target.files[0];
    evento.target.value = '';
    if (!elegido) return;
    try {
      const resumen = await administracionApi.cargarEscenario(elegido);
      avisos.exito(`${resumen.apartamentos} apartamentos y ${resumen.reservas} reservas cargados desde tu archivo.`, 'Escenario cargado');
    } catch {
      // aviso ya mostrado
    }
  }

  return (
    <div className="bento">
      <div className="c6">
        <Bisel>
          <div className="pila">
            <div className="fila separado">
              <h2 className="titulo-seccion">
                Reloj del <em>sistema</em>
              </h2>
              <Insignia tono={estado?.simulado ? 'ambar' : 'salvia'}>{estado?.simulado ? 'Simulado' : 'Tiempo real'}</Insignia>
            </div>
            <p className="cifra" style={{ fontSize: '2rem' }}>
              {estado ? formatearFechaHora(estado.ahora) : '—'}
            </p>
            <p className="tenue">Mueve el reloj para probar vencimientos de anticipo, llegadas y salidas sin esperar.</p>
            <label className="campo">
              <span>Viajar a</span>
              <input type="datetime-local" value={fecha} onChange={(e) => setFecha(e.target.value)} />
            </label>
            <div className="fila">
              <Boton icono={Clock} disabled={!fecha} onClick={viajar}>
                Viajar
              </Boton>
              <Boton variante="suave" icono={ArrowCounterClockwise} disabled={!estado?.simulado} onClick={reiniciar}>
                Volver a la hora real
              </Boton>
            </div>
          </div>
        </Bisel>
      </div>
      <div className="c6">
        <Bisel>
          <div className="pila">
            <h2 className="titulo-seccion">
              Escenarios de <em>datos</em>
            </h2>
            <p className="tenue">Carga el escenario de demostración o tu propio archivo para reiniciar el sistema con otros datos.</p>
            <div className="nota nota--aviso">Cargar un escenario reemplaza los datos actuales.</div>
            <div className="fila">
              <Boton variante="salvia" onClick={cargarPorDefecto}>
                Cargar demostración
              </Boton>
              <Boton variante="suave" icono={UploadSimple} onClick={() => archivo.current.click()}>
                Subir archivo
              </Boton>
              <input ref={archivo} type="file" hidden onChange={subir} />
            </div>
            <a className="enlace" href={administracionApi.urlPlantillaEscenario()} download>
              <DownloadSimple weight="light" /> Descargar plantilla de escenario
            </a>
          </div>
        </Bisel>
      </div>
    </div>
  );
}

const PESTANAS = [
  ['personal', 'Personal', Personal],
  ['temporadas', 'Temporadas', Temporadas],
  ['sistema', 'Sistema', Sistema],
];

/** Administración: personal, temporadas tarifarias y herramientas del sistema. */
export default function Administracion() {
  const [activa, setActiva] = useState('personal');
  const Pestana = PESTANAS.find(([clave]) => clave === activa)[2];
  return (
    <div className="pagina">
      <Encabezado eyebrow="Administración" titulo={<>Todo lo que <em>sostiene</em> al hotel</>} descripcion="Quién trabaja, cómo se cobra cada temporada y las herramientas para probar el sistema." />
      <div className="segmentos" role="tablist">
        {PESTANAS.map(([clave, etiqueta]) => (
          <button key={clave} type="button" role="tab" className="segmento" aria-selected={activa === clave} onClick={() => setActiva(clave)}>
            {etiqueta}
          </button>
        ))}
      </div>
      <Revelar key={activa}>
        <Pestana />
      </Revelar>
    </div>
  );
}
