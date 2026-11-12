import { ArrowCounterClockwise, Prohibit, SignIn, SignOut, Warning } from '@phosphor-icons/react';
import { useActionState, useState } from 'react';
import { useAvisos } from '../contexto/Avisos.jsx';
import { useReloj } from '../contexto/Reloj.jsx';
import { ROLES, useSesion } from '../contexto/Sesion.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { reservasApi } from '../servicios/api.js';
import { TONO_RESERVA } from '../utilidades/estados.js';
import { formatearDinero } from '../utilidades/dinero.js';
import { formatearFechaHora, formatearHora, formatearRango, tiempoRestante } from '../utilidades/fechas.js';
import { Boton } from './Boton.jsx';
import { Cargando } from './Estados.jsx';
import { Insignia } from './Insignia.jsx';
import { Modal } from './Modal.jsx';

/**
 * Contenido del detalle de una reserva: datos, ocupantes, estado de pago, movimientos del folio y
 * las acciones que permite el estado actual (ingreso, salida, no presentado, cancelar, cargos y pagos).
 * Los huéspedes solo pueden cancelar; el personal puede hacer todo.
 */
function Contenido({ id, alCambiar }) {
  const avisos = useAvisos();
  const { estado: reloj } = useReloj();
  const { rol } = useSesion();
  const esPersonal = rol !== ROLES.HUESPED;
  const [ocupado, setOcupado] = useState(false);
  const [vistaReembolso, setVistaReembolso] = useState(null);

  const { datos, recargar } = useConsulta(async () => {
    const [reserva, folio] = await Promise.all([reservasApi.buscar(id), reservasApi.folio(id)]);
    return { reserva, folio };
  }, [id]);

  /** Ejecuta una acción de la API, avisa el resultado y recarga el detalle y la pantalla de origen. */
  async function ejecutar(accion, mensaje) {
    setOcupado(true);
    try {
      await accion();
      avisos.exito(mensaje);
      setVistaReembolso(null);
      recargar();
      alCambiar?.();
    } catch {
      // el cliente de la API ya mostró el error como aviso
    } finally {
      setOcupado(false);
    }
  }

  const [, registrarMovimiento, registrando] = useActionState(async (_previo, formulario) => {
    const cuerpo = { monto: Number(formulario.get('monto')), descripcion: formulario.get('descripcion') || undefined };
    const esPago = formulario.get('tipo') === 'PAGO';
    await ejecutar(() => (esPago ? reservasApi.agregarPago(id, cuerpo) : reservasApi.agregarCargo(id, cuerpo)), esPago ? 'Pago registrado' : 'Cargo registrado');
    return null;
  }, null);

  if (!datos) return <Cargando bloques={3} />;
  const { reserva, folio } = datos;
  const abierta = reserva.estado === 'PENDIENTE' || reserva.estado === 'CONFIRMADA' || reserva.estado === 'EN_CURSO';
  const avancePago = reserva.total ? Math.min(1, reserva.pagado / reserva.total) : 0;

  async function pedirVistaPrevia() {
    try {
      setVistaReembolso(await reservasApi.vistaPreviaReembolso(id));
    } catch {
      // aviso ya mostrado
    }
  }

  return (
    <>
      <div className="fila">
        <Insignia tono={TONO_RESERVA[reserva.estado]}>{reserva.etiquetaEstado}</Insignia>
        <Insignia>{reserva.etiquetaCanal}</Insignia>
        {reserva.estado === 'PENDIENTE' && reloj && (
          <Insignia tono="ambar">Vence en {tiempoRestante(reserva.fechaVencimiento, reloj.ahora)}</Insignia>
        )}
      </div>

      <dl className="datos">
        <div>
          <dt>Apartamento</dt>
          <dd>
            {reserva.nombreApartamento} · {reserva.codigoApartamento}
          </dd>
        </div>
        <div>
          <dt>Titular</dt>
          <dd>{reserva.nombreTitular}</dd>
        </div>
        <div>
          <dt>Estancia</dt>
          <dd>
            {formatearRango(reserva.ingreso, reserva.salida)} · {reserva.noches} {reserva.noches === 1 ? 'noche' : 'noches'}
          </dd>
        </div>
        <div>
          <dt>Llegada estimada</dt>
          <dd>{formatearHora(reserva.llegadaEstimada)}</dd>
        </div>
      </dl>

      <div className="fichas">
        {reserva.ocupantes.map((ocupante) => (
          <span key={ocupante.nombreCompleto} className="ficha">
            {ocupante.nombreCompleto}
          </span>
        ))}
      </div>

      <div className="pila">
        <div className="fila separado">
          <span className="eyebrow">Estado de cuenta</span>
          <span className="pequeno tenue">Folio {folio.numero}</span>
        </div>
        <div className="barra" aria-hidden="true">
          <div className="barra__relleno" style={{ '--valor': avancePago }} />
        </div>
        <dl className="datos">
          <div>
            <dt>Total de la estancia</dt>
            <dd>{formatearDinero(reserva.total)}</dd>
          </div>
          <div>
            <dt>Anticipo requerido</dt>
            <dd>{formatearDinero(reserva.anticipoRequerido)}</dd>
          </div>
          <div>
            <dt>Pagado</dt>
            <dd>{formatearDinero(folio.pagado)}</dd>
          </div>
          <div>
            <dt>Saldo pendiente</dt>
            <dd>{formatearDinero(folio.saldo)}</dd>
          </div>
        </dl>
        {folio.movimientos.length > 0 && (
          <div className="tabla-contenedor">
            <table className="tabla">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Movimiento</th>
                  <th>Fecha</th>
                  <th className="derecha">Monto</th>
                </tr>
              </thead>
              <tbody>
                {folio.movimientos.map((movimiento) => (
                  <tr key={movimiento.numero}>
                    <td className="numero tenue">{movimiento.numero}</td>
                    <td>
                      <div className="celda-doble">
                        {movimiento.etiquetaTipo}
                        <small>{movimiento.descripcion}</small>
                      </div>
                    </td>
                    <td className="tenue pequeno">{formatearFechaHora(movimiento.fechaCreacion)}</td>
                    <td className="derecha numero">{formatearDinero(movimiento.montoConSigno)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {esPersonal && abierta && (
        <form action={registrarMovimiento} className="formulario">
          <label className="campo">
            <span>Tipo</span>
            <select name="tipo" defaultValue="PAGO">
              <option value="PAGO">Pago</option>
              <option value="CARGO">Cargo extra</option>
            </select>
          </label>
          <label className="campo">
            <span>Monto (COP)</span>
            <input name="monto" type="number" min="1" step="1000" required placeholder="50000" />
          </label>
          <label className="campo">
            <span>Descripción</span>
            <input name="descripcion" placeholder="Opcional" />
          </label>
          <div className="formulario__pie">
            {folio.puedeDeshacer && (
              <Boton variante="fantasma" icono={ArrowCounterClockwise} disabled={ocupado} onClick={() => ejecutar(() => reservasApi.deshacerUltimo(id), 'Movimiento deshecho')}>
                Deshacer: {folio.proximoDeshacer}
              </Boton>
            )}
            <Boton type="submit" variante="salvia" disabled={registrando || ocupado}>
              Registrar
            </Boton>
          </div>
        </form>
      )}

      {vistaReembolso && (
        <div className="nota nota--aviso">
          <Warning weight="light" />
          <div>
            <strong>Si cancelas ahora</strong>
            <p>
              Se devuelve el {vistaReembolso.porcentaje}% de lo pagado: {formatearDinero(vistaReembolso.reembolso)}. Penalidad de {formatearDinero(vistaReembolso.penalidad)} (faltan{' '}
              {vistaReembolso.diasAntesIngreso} días para el ingreso).
            </p>
          </div>
        </div>
      )}

      <div className="encabezado__acciones" style={{ justifyContent: 'flex-end' }}>
        {abierta && !vistaReembolso && (
          <Boton variante="peligro" icono={Prohibit} disabled={ocupado} onClick={pedirVistaPrevia}>
            Cancelar reserva
          </Boton>
        )}
        {vistaReembolso && (
          <>
            <Boton variante="suave" onClick={() => setVistaReembolso(null)}>
              Mantener
            </Boton>
            <Boton variante="peligro" disabled={ocupado} onClick={() => ejecutar(() => reservasApi.cancelar(id), 'Reserva cancelada')}>
              Confirmar cancelación
            </Boton>
          </>
        )}
        {esPersonal && reserva.estado === 'CONFIRMADA' && (
          <>
            <Boton variante="suave" disabled={ocupado} onClick={() => ejecutar(() => reservasApi.marcarNoPresentado(id), 'Marcada como no presentada')}>
              No se presentó
            </Boton>
            <Boton isla={SignIn} disabled={ocupado} onClick={() => ejecutar(() => reservasApi.registrarIngreso(id), 'Ingreso registrado')}>
              Registrar ingreso
            </Boton>
          </>
        )}
        {esPersonal && reserva.estado === 'EN_CURSO' && (
          <Boton isla={SignOut} disabled={ocupado} onClick={() => ejecutar(() => reservasApi.registrarSalida(id), 'Salida registrada')}>
            Registrar salida
          </Boton>
        )}
      </div>
    </>
  );
}

/**
 * Modal con el detalle de una reserva. `id` en `null` lo mantiene cerrado.
 * `alCambiar` se llama tras cualquier modificación para que la pantalla de origen recargue su lista.
 */
export function DetalleReserva({ id, alCerrar, alCambiar }) {
  return (
    <Modal abierto={Boolean(id)} alCerrar={alCerrar} eyebrow="Reserva" titulo={id ?? ''} ancho={720}>
      {id && <Contenido id={id} alCambiar={alCambiar} />}
    </Modal>
  );
}
