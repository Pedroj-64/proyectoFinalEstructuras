import { Door, MagnifyingGlass, Plus } from '@phosphor-icons/react';
import { useDeferredValue, useState } from 'react';
import { Bisel } from '../componentes/Bisel.jsx';
import { Boton } from '../componentes/Boton.jsx';
import { DetalleReserva } from '../componentes/DetalleReserva.jsx';
import { Encabezado } from '../componentes/Encabezado.jsx';
import { Cargando, Vacio } from '../componentes/Estados.jsx';
import { Insignia } from '../componentes/Insignia.jsx';
import { Revelar } from '../componentes/Revelar.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { useNavegacion } from '../navegacion/Navegacion.jsx';
import { reservasApi } from '../servicios/api.js';
import { formatearDinero } from '../utilidades/dinero.js';
import { CANALES, ESTADOS_RESERVA, TONO_RESERVA } from '../utilidades/estados.js';
import { formatearRango } from '../utilidades/fechas.js';

const ETIQUETA_ESTADO = { PENDIENTE: 'Pendientes', CONFIRMADA: 'Confirmadas', EN_CURSO: 'En curso', FINALIZADA: 'Finalizadas', CANCELADA: 'Canceladas', NO_PRESENTADO: 'No presentadas' };
const ETIQUETA_CANAL = { PORTAL: 'Portal', DIRECTO: 'Directa', EXTERNO: 'Externa' };

/**
 * Listado de reservas con filtros. Estado y canal se filtran en el servidor; la búsqueda por texto
 * se aplica en el navegador sobre lo ya cargado (con `useDeferredValue` para que escribir no se trabe).
 * Puede abrirse directamente en el detalle de una reserva con `parametros.abrir`.
 */
export default function Reservas({ parametros }) {
  const { ir } = useNavegacion();
  const [estado, setEstado] = useState('');
  const [canal, setCanal] = useState('');
  const [texto, setTexto] = useState('');
  const [abierta, setAbierta] = useState(parametros.abrir ?? null);
  const textoDiferido = useDeferredValue(texto.trim().toLowerCase());

  const { datos, cargando, actualizando, recargar } = useConsulta(() => reservasApi.listar({ estado, canal }), [estado, canal]);

  const filas = (datos ?? [])
    .filter((r) => !textoDiferido || `${r.id} ${r.nombreTitular} ${r.nombreApartamento} ${r.codigoApartamento}`.toLowerCase().includes(textoDiferido))
    .sort((a, b) => b.ingreso.localeCompare(a.ingreso));

  return (
    <div className="pagina">
      <Encabezado eyebrow="Operación" titulo={<>Todas las <em>reservas</em></>} descripcion="Filtra por estado o canal, abre una reserva para ver su folio, registrar pagos o hacer el ingreso y la salida.">
        <Boton isla={Plus} onClick={() => ir('descubrir')}>
          Nueva reserva
        </Boton>
      </Encabezado>

      <Revelar className="seccion">
        <div className="fila separado">
          <div className="segmentos">
            <button type="button" className="segmento" aria-pressed={estado === ''} onClick={() => setEstado('')}>
              Todas
            </button>
            {ESTADOS_RESERVA.map((valor) => (
              <button key={valor} type="button" className="segmento" aria-pressed={estado === valor} onClick={() => setEstado(valor)}>
                {ETIQUETA_ESTADO[valor]}
              </button>
            ))}
          </div>
          <div className="fila">
            <label className="campo" style={{ minWidth: '10rem' }}>
              <select aria-label="Canal" value={canal} onChange={(evento) => setCanal(evento.target.value)}>
                <option value="">Todos los canales</option>
                {CANALES.map((valor) => (
                  <option key={valor} value={valor}>
                    {ETIQUETA_CANAL[valor]}
                  </option>
                ))}
              </select>
            </label>
            <label className="campo buscador">
              <MagnifyingGlass weight="light" />
              <input type="search" placeholder="Buscar por titular o apartamento" value={texto} onChange={(evento) => setTexto(evento.target.value)} />
            </label>
          </div>
        </div>

        <Bisel>
          {cargando && <Cargando bloques={4} />}
          {datos && filas.length === 0 && <Vacio icono={Door} titulo="Sin resultados" texto="Ninguna reserva coincide con los filtros." />}
          {filas.length > 0 && (
            <div className="tabla-contenedor" style={{ opacity: actualizando ? 0.5 : 1, transition: 'opacity 0.5s var(--muelle)' }}>
              <table className="tabla">
                <thead>
                  <tr>
                    <th>Reserva</th>
                    <th>Titular</th>
                    <th>Apartamento</th>
                    <th>Estancia</th>
                    <th>Estado</th>
                    <th className="derecha">Saldo</th>
                  </tr>
                </thead>
                <tbody>
                  {filas.map((reserva) => (
                    <tr key={reserva.id} className="clicable" onClick={() => setAbierta(reserva.id)}>
                      <td>
                        <div className="celda-doble">
                          <strong>{reserva.id}</strong>
                          <small>{reserva.etiquetaCanal}</small>
                        </div>
                      </td>
                      <td>{reserva.nombreTitular}</td>
                      <td>
                        <div className="celda-doble">
                          {reserva.nombreApartamento}
                          <small>{reserva.codigoApartamento}</small>
                        </div>
                      </td>
                      <td className="sin-corte">{formatearRango(reserva.ingreso, reserva.salida)}</td>
                      <td>
                        <Insignia tono={TONO_RESERVA[reserva.estado]}>{reserva.etiquetaEstado}</Insignia>
                      </td>
                      <td className="derecha numero">{formatearDinero(reserva.saldo)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Bisel>
      </Revelar>

      <DetalleReserva id={abierta} alCerrar={() => setAbierta(null)} alCambiar={recargar} />
    </div>
  );
}
