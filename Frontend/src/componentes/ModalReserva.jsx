import { ArrowRight, Minus, Plus } from '@phosphor-icons/react';
import { useState } from 'react';
import { useAvisos } from '../contexto/Avisos.jsx';
import { ROLES, useSesion } from '../contexto/Sesion.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { huespedesApi, reservasApi } from '../servicios/api.js';
import { formatearDinero } from '../utilidades/dinero.js';
import { fechaCorta, formatearRango } from '../utilidades/fechas.js';
import { Boton } from './Boton.jsx';
import { Cargando } from './Estados.jsx';
import { Insignia } from './Insignia.jsx';
import { Modal } from './Modal.jsx';
import { TONO_TEMPORADA } from '../utilidades/estados.js';

const OCUPANTE_VACIO = { nombreCompleto: '', fechaNacimiento: '' };

/**
 * Contenido del modal de reserva: elige titular y ocupantes, muestra la cotización noche por noche
 * (los menores no pagan, lo decide el backend) y crea la reserva.
 */
function Contenido({ reserva, alCreada }) {
  const avisos = useAvisos();
  const { rol, documentoHuesped } = useSesion();
  const esHuesped = rol === ROLES.HUESPED;
  const { apartamento, ingreso, salida, huespedes } = reserva;

  const { datos: listaHuespedes } = useConsulta(huespedesApi.listar);
  const [documento, setDocumento] = useState(documentoHuesped);
  const [extras, setExtras] = useState(() => Array.from({ length: Math.max(0, huespedes - 1) }, () => ({ ...OCUPANTE_VACIO })));
  const [llegada, setLlegada] = useState('15:00');
  const [enviando, setEnviando] = useState(false);

  const titular = (listaHuespedes ?? []).find((huesped) => huesped.documento === documento);
  const completos = extras.filter((ocupante) => ocupante.nombreCompleto.trim() && ocupante.fechaNacimiento);
  const ocupantes = titular ? [{ nombreCompleto: titular.nombreCompleto, fechaNacimiento: titular.fechaNacimiento }, ...completos] : [];
  const incompletos = extras.length - completos.length;

  const { datos: cotizacion, actualizando } = useConsulta(
    () => (ocupantes.length ? reservasApi.cotizar(apartamento.codigo, ingreso, salida, ocupantes) : Promise.resolve(null)),
    [documento, titular?.documento, JSON.stringify(completos)],
  );

  const cambiarExtra = (indice, campo, valor) => setExtras((lista) => lista.map((ocupante, posicion) => (posicion === indice ? { ...ocupante, [campo]: valor } : ocupante)));

  async function confirmar() {
    setEnviando(true);
    try {
      const creada = await reservasApi.crear({
        codigoApartamento: apartamento.codigo,
        documentoTitular: documento,
        ocupantes,
        ingreso,
        salida,
        canal: esHuesped ? 'PORTAL' : 'DIRECTO',
        llegadaEstimada: `${llegada}:00`,
      });
      avisos.exito(`Reserva ${creada.id} creada en ${apartamento.nombre}`, 'Reserva confirmada');
      alCreada(creada);
    } catch {
      // el cliente de la API ya mostró el error
    } finally {
      setEnviando(false);
    }
  }

  return (
    <>
      <div className="fila">
        <Insignia tono="salvia">{apartamento.codigo}</Insignia>
        <Insignia>{formatearRango(ingreso, salida)}</Insignia>
        <Insignia>Capacidad {apartamento.capacidad}</Insignia>
      </div>

      <div className="formulario formulario--ancho">
        {!esHuesped && (
          <label className="campo">
            <span>Titular de la reserva</span>
            <select value={documento} onChange={(evento) => setDocumento(evento.target.value)}>
              {(listaHuespedes ?? []).map((huesped) => (
                <option key={huesped.documento} value={huesped.documento}>
                  {huesped.nombreCompleto}
                </option>
              ))}
            </select>
          </label>
        )}
        <label className="campo">
          <span>Hora estimada de llegada</span>
          <input type="time" value={llegada} onChange={(evento) => setLlegada(evento.target.value)} />
        </label>
      </div>

      <div className="pila">
        <div className="fila separado">
          <span className="eyebrow">Ocupantes</span>
          <div className="fila">
            <Boton variante="suave" pequeno soloIcono aria-label="Quitar ocupante" disabled={!extras.length} onClick={() => setExtras((lista) => lista.slice(0, -1))}>
              <Minus weight="light" />
            </Boton>
            <span className="numero">{extras.length + 1}</span>
            <Boton variante="suave" pequeno soloIcono aria-label="Añadir ocupante" disabled={extras.length + 1 >= apartamento.capacidad} onClick={() => setExtras((lista) => [...lista, { ...OCUPANTE_VACIO }])}>
              <Plus weight="light" />
            </Boton>
          </div>
        </div>
        <p className="pequeno tenue">{titular ? `${titular.nombreCompleto} (titular) ya está incluido.` : 'Cargando titular…'}</p>
        {extras.map((ocupante, indice) => (
          <div key={indice} className="formulario">
            <label className="campo">
              <span>Nombre completo</span>
              <input value={ocupante.nombreCompleto} onChange={(evento) => cambiarExtra(indice, 'nombreCompleto', evento.target.value)} placeholder="Nombre y apellido" />
            </label>
            <label className="campo">
              <span>Fecha de nacimiento</span>
              <input type="date" value={ocupante.fechaNacimiento} onChange={(evento) => cambiarExtra(indice, 'fechaNacimiento', evento.target.value)} />
            </label>
          </div>
        ))}
      </div>

      <div className="pila" style={{ opacity: actualizando ? 0.5 : 1, transition: 'opacity 0.5s var(--muelle)' }}>
        <span className="eyebrow">Cotización</span>
        {!cotizacion && <Cargando bloques={1} />}
        {cotizacion && (
          <>
            <div className="tabla-contenedor">
              <table className="tabla">
                <thead>
                  <tr>
                    <th>Noche</th>
                    <th>Temporada</th>
                    <th className="derecha">Pagan</th>
                    <th className="derecha">Subtotal</th>
                  </tr>
                </thead>
                <tbody>
                  {cotizacion.desglose.map((noche) => (
                    <tr key={noche.noche}>
                      <td>{fechaCorta(noche.noche)}</td>
                      <td>
                        <Insignia tono={TONO_TEMPORADA[noche.temporada]}>{noche.etiquetaTemporada}</Insignia>
                      </td>
                      <td className="derecha numero">
                        {noche.huespedesQuePagan}
                        {noche.huespedesGratis > 0 && <span className="tenue"> (+{noche.huespedesGratis} gratis)</span>}
                      </td>
                      <td className="derecha numero">{formatearDinero(noche.subtotal)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <dl className="datos">
              <div>
                <dt>Total</dt>
                <dd>{formatearDinero(cotizacion.total)}</dd>
              </div>
              <div>
                <dt>Anticipo para confirmar</dt>
                <dd>{formatearDinero(cotizacion.anticipo)}</dd>
              </div>
            </dl>
          </>
        )}
      </div>

      {incompletos > 0 && <div className="nota nota--aviso">Completa el nombre y la fecha de nacimiento de los {incompletos} ocupante(s) restantes, o quita las filas que no uses.</div>}

      <div className="encabezado__acciones" style={{ justifyContent: 'flex-end' }}>
        <Boton isla={ArrowRight} disabled={!cotizacion || incompletos > 0 || enviando} onClick={confirmar}>
          Confirmar reserva
        </Boton>
      </div>
    </>
  );
}

/**
 * Modal de reserva de un apartamento. `reserva` es `{ apartamento, ingreso, salida, huespedes }`
 * (o `null` para mantenerlo cerrado); `alCreada` recibe la reserva ya creada.
 */
export function ModalReserva({ reserva, alCerrar, alCreada }) {
  return (
    <Modal abierto={Boolean(reserva)} alCerrar={alCerrar} eyebrow="Nueva reserva" titulo={reserva?.apartamento.nombre ?? ''} ancho={760}>
      {reserva && <Contenido reserva={reserva} alCreada={alCreada} />}
    </Modal>
  );
}
