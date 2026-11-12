import { MagnifyingGlass, PencilSimple, Plus, Trash } from '@phosphor-icons/react';
import { useActionState, useDeferredValue, useState } from 'react';
import { Bisel } from '../componentes/Bisel.jsx';
import { Boton } from '../componentes/Boton.jsx';
import { Encabezado } from '../componentes/Encabezado.jsx';
import { Cargando, Vacio } from '../componentes/Estados.jsx';
import { Modal } from '../componentes/Modal.jsx';
import { Revelar } from '../componentes/Revelar.jsx';
import { useAvisos } from '../contexto/Avisos.jsx';
import { ROLES, useSesion } from '../contexto/Sesion.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { huespedesApi } from '../servicios/api.js';
import { formatearDinero } from '../utilidades/dinero.js';
import { iniciales } from '../utilidades/estados.js';
import { formatearFecha, formatearRango } from '../utilidades/fechas.js';

/** Formulario de registro o edición de un huésped. */
function FormularioHuesped({ huesped, alTerminar }) {
  const avisos = useAvisos();
  const editando = Boolean(huesped);

  const [, accion, enviando] = useActionState(async (_previo, formulario) => {
    const solicitud = {
      documento: formulario.get('documento').trim(),
      nombreCompleto: formulario.get('nombre').trim(),
      fechaNacimiento: formulario.get('nacimiento'),
      telefono: formulario.get('telefono') || null,
      correo: formulario.get('correo') || null,
      urlFoto: null,
    };
    try {
      if (editando) await huespedesApi.actualizar(huesped.documento, solicitud);
      else await huespedesApi.registrar(solicitud);
      avisos.exito(editando ? 'Huésped actualizado' : 'Huésped registrado');
      alTerminar();
    } catch {
      // aviso ya mostrado
    }
    return null;
  }, null);

  return (
    <form action={accion} className="formulario formulario--ancho">
      <label className="campo">
        <span>Documento</span>
        <input name="documento" required defaultValue={huesped?.documento} readOnly={editando} placeholder="1094000099" />
      </label>
      <label className="campo">
        <span>Nombre completo</span>
        <input name="nombre" required defaultValue={huesped?.nombreCompleto} placeholder="Nombre y apellido" />
      </label>
      <label className="campo">
        <span>Fecha de nacimiento</span>
        <input name="nacimiento" type="date" required defaultValue={huesped?.fechaNacimiento} />
      </label>
      <label className="campo">
        <span>Teléfono</span>
        <input name="telefono" defaultValue={huesped?.telefono ?? ''} />
      </label>
      <label className="campo" style={{ gridColumn: '1 / -1' }}>
        <span>Correo</span>
        <input name="correo" type="email" defaultValue={huesped?.correo ?? ''} />
      </label>
      <div className="formulario__pie">
        <Boton variante="suave" onClick={alTerminar}>
          Cancelar
        </Boton>
        <Boton type="submit" variante="salvia" disabled={enviando}>
          {editando ? 'Guardar cambios' : 'Registrar'}
        </Boton>
      </div>
    </form>
  );
}

/** Ficha de un huésped: datos de contacto e historial de estancias. */
function FichaHuesped({ huesped }) {
  const { datos } = useConsulta(() => huespedesApi.historial(huesped.documento), [huesped.documento]);
  return (
    <>
      <dl className="datos">
        <div>
          <dt>Documento</dt>
          <dd>{huesped.documento}</dd>
        </div>
        <div>
          <dt>Nacimiento</dt>
          <dd>{formatearFecha(huesped.fechaNacimiento)}</dd>
        </div>
        <div>
          <dt>Teléfono</dt>
          <dd>{huesped.telefono ?? '—'}</dd>
        </div>
        <div>
          <dt>Correo</dt>
          <dd>{huesped.correo ?? '—'}</dd>
        </div>
        <div>
          <dt>Favoritos</dt>
          <dd>{huesped.favoritos.length ? huesped.favoritos.join(', ') : '—'}</dd>
        </div>
      </dl>
      <span className="eyebrow">Historial de estancias</span>
      {!datos && <Cargando bloques={1} />}
      {datos?.length === 0 && <p className="tenue">Sin estancias completadas.</p>}
      {datos?.length > 0 && (
        <ul className="lista">
          {datos.map((estancia) => (
            <li key={estancia.idReserva} className="fila-lista fila-lista--fija">
              <div className="celda-doble">
                <strong>{estancia.nombreApartamento}</strong>
                <small>
                  {formatearRango(estancia.ingreso, estancia.salida)} · {estancia.noches} noches
                </small>
              </div>
              <span className="numero">{formatearDinero(estancia.total)}</span>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}

/** Directorio de huéspedes: búsqueda, ficha con historial y alta, edición y baja. */
export default function Huespedes() {
  const { rol } = useSesion();
  const avisos = useAvisos();
  const esAdmin = rol === ROLES.ADMINISTRADOR;
  const [texto, setTexto] = useState('');
  const textoDiferido = useDeferredValue(texto.trim().toLowerCase());
  const [ficha, setFicha] = useState(null);
  const [edicion, setEdicion] = useState(null); // `{}` = registrar

  const { datos, cargando, recargar } = useConsulta(huespedesApi.listar);
  const lista = (datos ?? []).filter((h) => !textoDiferido || `${h.nombreCompleto} ${h.documento} ${h.correo ?? ''}`.toLowerCase().includes(textoDiferido));

  async function eliminar(huesped) {
    if (!window.confirm(`¿Eliminar a ${huesped.nombreCompleto}?`)) return;
    try {
      await huespedesApi.eliminar(huesped.documento);
      avisos.exito('Huésped eliminado');
      setFicha(null);
      recargar();
    } catch {
      // aviso ya mostrado
    }
  }

  return (
    <div className="pagina">
      <Encabezado eyebrow="Clientes" titulo={<>Nuestros <em>huéspedes</em></>} descripcion="Quién se ha hospedado, cuántas veces y qué apartamentos prefiere.">
        <Boton isla={Plus} onClick={() => setEdicion({})}>
          Registrar huésped
        </Boton>
      </Encabezado>

      <label className="campo buscador" style={{ maxWidth: '26rem' }}>
        <MagnifyingGlass weight="light" />
        <input type="search" placeholder="Buscar por nombre, documento o correo" value={texto} onChange={(evento) => setTexto(evento.target.value)} />
      </label>

      {cargando && <Cargando bloques={3} />}
      {datos && lista.length === 0 && <Vacio titulo="Sin coincidencias" texto="Prueba con otro nombre o documento." />}

      <div className="rejilla">
        {lista.map((huesped, indice) => (
          <Revelar key={huesped.documento} retraso={(indice % 3) * 90}>
            <Bisel compacto interactivo>
              <button type="button" className="tarjeta-huesped" onClick={() => setFicha(huesped)}>
                <span className="avatar">{iniciales(huesped.nombreCompleto)}</span>
                <div className="celda-doble" style={{ textAlign: 'left' }}>
                  <strong>{huesped.nombreCompleto}</strong>
                  <small>
                    {huesped.documento} · {huesped.cantidadEstancias} {huesped.cantidadEstancias === 1 ? 'estancia' : 'estancias'}
                  </small>
                </div>
              </button>
            </Bisel>
          </Revelar>
        ))}
      </div>

      <Modal abierto={Boolean(ficha)} alCerrar={() => setFicha(null)} eyebrow="Huésped" titulo={ficha?.nombreCompleto ?? ''} ancho={620}>
        {ficha && (
          <>
            <FichaHuesped huesped={ficha} />
            <div className="encabezado__acciones" style={{ justifyContent: 'flex-end' }}>
              {esAdmin && (
                <Boton variante="peligro" icono={Trash} onClick={() => eliminar(ficha)}>
                  Eliminar
                </Boton>
              )}
              <Boton
                variante="suave"
                icono={PencilSimple}
                onClick={() => {
                  setEdicion(ficha);
                  setFicha(null);
                }}
              >
                Editar datos
              </Boton>
            </div>
          </>
        )}
      </Modal>

      <Modal abierto={Boolean(edicion)} alCerrar={() => setEdicion(null)} eyebrow="Huésped" titulo={edicion?.documento ? 'Editar huésped' : 'Registrar huésped'} ancho={640}>
        {edicion && (
          <FormularioHuesped
            huesped={edicion.documento ? edicion : null}
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
