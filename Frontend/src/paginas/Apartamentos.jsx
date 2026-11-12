import { PencilSimple, Plus, Trash } from '@phosphor-icons/react';
import { useActionState, useState } from 'react';
import { Bisel } from '../componentes/Bisel.jsx';
import { Boton } from '../componentes/Boton.jsx';
import { Encabezado } from '../componentes/Encabezado.jsx';
import { Cargando } from '../componentes/Estados.jsx';
import { ImagenFoto } from '../componentes/ImagenFoto.jsx';
import { Insignia } from '../componentes/Insignia.jsx';
import { Modal } from '../componentes/Modal.jsx';
import { Revelar } from '../componentes/Revelar.jsx';
import { useAvisos } from '../contexto/Avisos.jsx';
import { ROLES, useSesion } from '../contexto/Sesion.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { apartamentosApi } from '../servicios/api.js';
import { formatearDinero } from '../utilidades/dinero.js';
import { ETIQUETA_APARTAMENTO, TONO_APARTAMENTO, TRANSICIONES_APARTAMENTO } from '../utilidades/estados.js';
import { fotoApartamento } from '../utilidades/fotos.js';

/** Formulario de creación/edición de un apartamento (solo administrador). Usa una acción de formulario de React 19. */
function FormularioApartamento({ apartamento, alTerminar }) {
  const avisos = useAvisos();
  const editando = Boolean(apartamento);

  const [, accion, enviando] = useActionState(async (_previo, formulario) => {
    const solicitud = {
      codigo: formulario.get('codigo').trim(),
      nombre: formulario.get('nombre').trim(),
      bloque: formulario.get('bloque').trim(),
      piso: Number(formulario.get('piso')),
      habitaciones: Number(formulario.get('habitaciones')),
      capacidad: Number(formulario.get('capacidad')),
      caracteristicas: formulario.get('caracteristicas').split(',').map((texto) => texto.trim()).filter(Boolean),
      tarifas: { baja: Number(formulario.get('baja')), media: Number(formulario.get('media')), alta: Number(formulario.get('alta')) },
    };
    try {
      if (editando) await apartamentosApi.actualizar(apartamento.codigo, solicitud);
      else await apartamentosApi.crear(solicitud);
      avisos.exito(editando ? 'Apartamento actualizado' : 'Apartamento creado');
      alTerminar();
    } catch {
      // aviso ya mostrado por el cliente de la API
    }
    return null;
  }, null);

  return (
    <form action={accion} className="formulario formulario--ancho">
      <label className="campo">
        <span>Código</span>
        <input name="codigo" required defaultValue={apartamento?.codigo} readOnly={editando} placeholder="A-304" />
      </label>
      <label className="campo">
        <span>Nombre</span>
        <input name="nombre" required defaultValue={apartamento?.nombre} placeholder="Suite Mirador" />
      </label>
      <label className="campo">
        <span>Bloque</span>
        <input name="bloque" defaultValue={apartamento?.bloque} placeholder="A" />
      </label>
      <label className="campo">
        <span>Piso</span>
        <input name="piso" type="number" min="1" defaultValue={apartamento?.piso ?? 1} />
      </label>
      <label className="campo">
        <span>Habitaciones</span>
        <input name="habitaciones" type="number" min="1" required defaultValue={apartamento?.habitaciones ?? 1} />
      </label>
      <label className="campo">
        <span>Capacidad</span>
        <input name="capacidad" type="number" min="1" required defaultValue={apartamento?.capacidad ?? 2} />
      </label>
      <label className="campo">
        <span>Tarifa baja (por persona/noche)</span>
        <input name="baja" type="number" min="0" step="1000" required defaultValue={apartamento?.tarifas.baja ?? 90000} />
      </label>
      <label className="campo">
        <span>Tarifa media</span>
        <input name="media" type="number" min="0" step="1000" required defaultValue={apartamento?.tarifas.media ?? 110000} />
      </label>
      <label className="campo">
        <span>Tarifa alta</span>
        <input name="alta" type="number" min="0" step="1000" required defaultValue={apartamento?.tarifas.alta ?? 140000} />
      </label>
      <label className="campo" style={{ gridColumn: '1 / -1' }}>
        <span>Características (separadas por coma)</span>
        <input name="caracteristicas" defaultValue={apartamento?.caracteristicas.join(', ')} placeholder="Balcón, Cocina equipada, Wifi" />
      </label>
      <div className="formulario__pie">
        <Boton variante="suave" onClick={alTerminar}>
          Cancelar
        </Boton>
        <Boton type="submit" variante="salvia" disabled={enviando}>
          {editando ? 'Guardar cambios' : 'Crear apartamento'}
        </Boton>
      </div>
    </form>
  );
}

/** Formulario para cambiar el estado operativo; el motivo solo se pide al pasar a "fuera de servicio". */
function FormularioEstado({ apartamento, destino, alTerminar }) {
  const avisos = useAvisos();
  const [, accion, enviando] = useActionState(async (_previo, formulario) => {
    try {
      await apartamentosApi.cambiarEstado(apartamento.codigo, destino, formulario.get('motivo') || undefined);
      avisos.exito(`${apartamento.nombre}: ${ETIQUETA_APARTAMENTO[destino].toLowerCase()}`);
      alTerminar(true);
    } catch {
      // aviso ya mostrado
    }
    return null;
  }, null);

  return (
    <form action={accion} className="pila">
      {destino === 'FUERA_DE_SERVICIO' ? (
        <label className="campo">
          <span>Motivo (obligatorio)</span>
          <textarea name="motivo" required placeholder="Ej.: fuga en el baño principal" />
        </label>
      ) : (
        <p className="tenue">El apartamento pasará de «{ETIQUETA_APARTAMENTO[apartamento.estado]}» a «{ETIQUETA_APARTAMENTO[destino]}».</p>
      )}
      <div className="formulario__pie" style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
        <Boton variante="suave" onClick={() => alTerminar(false)}>
          Cancelar
        </Boton>
        <Boton type="submit" disabled={enviando}>
          Confirmar
        </Boton>
      </div>
    </form>
  );
}

/** Catálogo de apartamentos: estado operativo, tarifas por temporada y, para el administrador, edición. */
export default function Apartamentos() {
  const { rol } = useSesion();
  const avisos = useAvisos();
  const esAdmin = rol === ROLES.ADMINISTRADOR;
  const [filtro, setFiltro] = useState('');
  const [edicion, setEdicion] = useState(null); // `{}` = crear, apartamento = editar
  const [cambio, setCambio] = useState(null); // `{ apartamento, destino }`

  const { datos, cargando, recargar } = useConsulta(apartamentosApi.listar);
  const lista = (datos ?? []).filter((apartamento) => !filtro || apartamento.estado === filtro);

  async function eliminar(apartamento) {
    if (!window.confirm(`¿Eliminar ${apartamento.nombre}? Esta acción no se puede deshacer.`)) return;
    try {
      await apartamentosApi.eliminar(apartamento.codigo);
      avisos.exito(`${apartamento.nombre} eliminado`);
      recargar();
    } catch {
      // aviso ya mostrado
    }
  }

  return (
    <div className="pagina">
      <Encabezado eyebrow="Inventario" titulo={<>Los <em>apartamentos</em></>} descripcion="Estado operativo en tiempo real y tarifas por persona y noche en cada temporada.">
        {esAdmin && (
          <Boton isla={Plus} onClick={() => setEdicion({})}>
            Nuevo apartamento
          </Boton>
        )}
      </Encabezado>

      <div className="segmentos">
        <button type="button" className="segmento" aria-pressed={filtro === ''} onClick={() => setFiltro('')}>
          Todos
        </button>
        {Object.keys(ETIQUETA_APARTAMENTO).map((estado) => (
          <button key={estado} type="button" className="segmento" aria-pressed={filtro === estado} onClick={() => setFiltro(estado)}>
            {ETIQUETA_APARTAMENTO[estado]}
          </button>
        ))}
      </div>

      {cargando && <Cargando bloques={3} />}

      <div className="rejilla">
        {lista.map((apartamento, indice) => (
          <Revelar key={apartamento.codigo} retraso={(indice % 3) * 100}>
            <Bisel sinRelleno interactivo className="tarjeta-reserva">
              <div className="tarjeta-reserva__foto">
                <ImagenFoto src={fotoApartamento(apartamento.codigo, 700)} alt={apartamento.nombre} />
                <Insignia tono={TONO_APARTAMENTO[apartamento.estado]}>{apartamento.etiquetaEstado}</Insignia>
              </div>
              <div className="tarjeta-apto__cuerpo">
                <div className="fila separado">
                  <div>
                    <h3 className="titulo-tarjeta">{apartamento.nombre}</h3>
                    <p className="tenue pequeno">
                      {apartamento.codigo} · piso {apartamento.piso} · {apartamento.habitaciones} hab. · hasta {apartamento.capacidad}
                    </p>
                  </div>
                  {esAdmin && (
                    <div className="fila" style={{ gap: '0.25rem' }}>
                      <Boton variante="fantasma" pequeno soloIcono aria-label="Editar" onClick={() => setEdicion(apartamento)}>
                        <PencilSimple weight="light" />
                      </Boton>
                      <Boton variante="fantasma" pequeno soloIcono aria-label="Eliminar" onClick={() => eliminar(apartamento)}>
                        <Trash weight="light" />
                      </Boton>
                    </div>
                  )}
                </div>
                <dl className="tarifas">
                  {[
                    ['Baja', apartamento.tarifas.baja],
                    ['Media', apartamento.tarifas.media],
                    ['Alta', apartamento.tarifas.alta],
                  ].map(([nombre, valor]) => (
                    <div key={nombre}>
                      <dt>{nombre}</dt>
                      <dd className="numero">{formatearDinero(valor)}</dd>
                    </div>
                  ))}
                </dl>
                {apartamento.motivoFueraDeServicio && <p className="pequeno" style={{ color: 'var(--terracota)' }}>Motivo: {apartamento.motivoFueraDeServicio}</p>}
                <div className="fila" style={{ gap: '0.4rem' }}>
                  {TRANSICIONES_APARTAMENTO[apartamento.estado].map((destino) => (
                    <Boton key={destino} variante={destino === 'FUERA_DE_SERVICIO' ? 'peligro' : 'suave'} pequeno onClick={() => setCambio({ apartamento, destino })}>
                      {ETIQUETA_APARTAMENTO[destino]}
                    </Boton>
                  ))}
                </div>
              </div>
            </Bisel>
          </Revelar>
        ))}
      </div>

      <Modal abierto={Boolean(edicion)} alCerrar={() => setEdicion(null)} eyebrow="Apartamento" titulo={edicion?.codigo ? `Editar ${edicion.nombre}` : 'Nuevo apartamento'} ancho={720}>
        {edicion && (
          <FormularioApartamento
            apartamento={edicion.codigo ? edicion : null}
            alTerminar={() => {
              setEdicion(null);
              recargar();
            }}
          />
        )}
      </Modal>

      <Modal abierto={Boolean(cambio)} alCerrar={() => setCambio(null)} eyebrow="Cambiar estado" titulo={cambio?.apartamento.nombre ?? ''} ancho={520}>
        {cambio && (
          <FormularioEstado
            {...cambio}
            alTerminar={(cambio_) => {
              setCambio(null);
              if (cambio_) recargar();
            }}
          />
        )}
      </Modal>
    </div>
  );
}
