import { PaperPlaneTilt, Plugs, Stack } from '@phosphor-icons/react';
import { useActionState, useState } from 'react';
import { Bisel } from '../componentes/Bisel.jsx';
import { Boton } from '../componentes/Boton.jsx';
import { Encabezado } from '../componentes/Encabezado.jsx';
import { Cargando, Vacio } from '../componentes/Estados.jsx';
import { Insignia } from '../componentes/Insignia.jsx';
import { Modal } from '../componentes/Modal.jsx';
import { Revelar } from '../componentes/Revelar.jsx';
import { useAvisos } from '../contexto/Avisos.jsx';
import { useReloj } from '../contexto/Reloj.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { apartamentosApi, operacionesApi } from '../servicios/api.js';
import { formatearRango, sumarDias } from '../utilidades/fechas.js';

const PLATAFORMAS = ['Booking', 'Airbnb', 'Expedia'];

/** Formulario que simula a una plataforma externa enviando una solicitud de reserva. */
function FormularioSolicitud({ alTerminar }) {
  const avisos = useAvisos();
  const { hoy } = useReloj();
  const { datos: apartamentos } = useConsulta(apartamentosApi.listar);

  const [, accion, enviando] = useActionState(async (_previo, formulario) => {
    const nombre = formulario.get('nombre').trim();
    const nacimiento = formulario.get('nacimiento');
    try {
      await operacionesApi.enviarSolicitudExterna({
        plataforma: formulario.get('plataforma'),
        codigoApartamento: formulario.get('apartamento'),
        ingreso: formulario.get('ingreso'),
        salida: formulario.get('salida'),
        titular: { documento: formulario.get('documento').trim(), nombreCompleto: nombre, fechaNacimiento: nacimiento, telefono: formulario.get('telefono') || null, correo: formulario.get('correo') || null, urlFoto: null },
        ocupantes: [{ nombreCompleto: nombre, fechaNacimiento: nacimiento }],
      });
      avisos.exito('Solicitud encolada. Procésala cuando quieras.');
      alTerminar();
    } catch {
      // aviso ya mostrado
    }
    return null;
  }, null);

  return (
    <form action={accion} className="formulario formulario--ancho">
      <label className="campo">
        <span>Plataforma</span>
        <select name="plataforma">
          {PLATAFORMAS.map((plataforma) => (
            <option key={plataforma}>{plataforma}</option>
          ))}
        </select>
      </label>
      <label className="campo">
        <span>Apartamento</span>
        <select name="apartamento" required>
          {(apartamentos ?? []).map((apartamento) => (
            <option key={apartamento.codigo} value={apartamento.codigo}>
              {apartamento.codigo} · {apartamento.nombre}
            </option>
          ))}
        </select>
      </label>
      <label className="campo">
        <span>Ingreso</span>
        <input name="ingreso" type="date" required defaultValue={sumarDias(hoy, 7)} />
      </label>
      <label className="campo">
        <span>Salida</span>
        <input name="salida" type="date" required defaultValue={sumarDias(hoy, 9)} />
      </label>
      <label className="campo">
        <span>Documento del titular</span>
        <input name="documento" required placeholder="1094000099" />
      </label>
      <label className="campo">
        <span>Nombre completo</span>
        <input name="nombre" required placeholder="Nombre y apellido" />
      </label>
      <label className="campo">
        <span>Fecha de nacimiento</span>
        <input name="nacimiento" type="date" required defaultValue="1990-01-01" />
      </label>
      <label className="campo">
        <span>Correo</span>
        <input name="correo" type="email" placeholder="opcional" />
      </label>
      <label className="campo">
        <span>Teléfono</span>
        <input name="telefono" placeholder="opcional" />
      </label>
      <div className="formulario__pie">
        <Boton variante="suave" onClick={alTerminar}>
          Cancelar
        </Boton>
        <Boton type="submit" variante="salvia" disabled={enviando}>
          Enviar a la cola
        </Boton>
      </div>
    </form>
  );
}

/** Canal externo: cola FIFO de solicitudes de plataformas, resultados de procesarlas y conflictos detectados. */
export default function CanalExterno() {
  const avisos = useAvisos();
  const [ocupado, setOcupado] = useState(false);
  const [simulando, setSimulando] = useState(false);

  const { datos, cargando, recargar } = useConsulta(async () => {
    const [pendientes, resultados, conflictos] = await Promise.all([operacionesApi.colaExterna(), operacionesApi.resultadosExternos(), operacionesApi.conflictosCanal()]);
    return { pendientes, resultados, conflictos };
  });

  async function procesar(accion, mensaje) {
    setOcupado(true);
    try {
      await accion();
      avisos.exito(mensaje);
      recargar();
    } catch {
      // aviso ya mostrado
    } finally {
      setOcupado(false);
    }
  }

  return (
    <div className="pagina">
      <Encabezado eyebrow="Integraciones" titulo={<>Canal <em>externo</em></>} descripcion="Las solicitudes de plataformas llegan a una cola y se atienden en orden de llegada. Si chocan con una reserva existente se registran como conflicto.">
        <Boton variante="suave" icono={PaperPlaneTilt} onClick={() => setSimulando(true)}>
          Simular solicitud
        </Boton>
        <Boton variante="suave" icono={Stack} disabled={ocupado || !datos?.pendientes.length} onClick={() => procesar(operacionesApi.procesarTodasExternas, 'Cola procesada')}>
          Procesar todas
        </Boton>
        <Boton isla={Plugs} disabled={ocupado || !datos?.pendientes.length} onClick={() => procesar(operacionesApi.procesarSiguienteExterna, 'Solicitud procesada')}>
          Procesar siguiente
        </Boton>
      </Encabezado>

      {cargando && <Cargando bloques={3} />}

      {datos && (
        <div className="bento">
          <Revelar className="c7">
            <Bisel>
              <div className="pila">
                <h2 className="titulo-seccion">
                  En <em>cola</em> <span className="tenue numero">({datos.pendientes.length})</span>
                </h2>
                {datos.pendientes.length === 0 ? (
                  <Vacio icono={Plugs} titulo="Cola vacía" texto="No hay solicitudes esperando." />
                ) : (
                  <div className="tabla-contenedor">
                    <table className="tabla">
                      <thead>
                        <tr>
                          <th>Plataforma</th>
                          <th>Apartamento</th>
                          <th>Estancia</th>
                          <th>Titular</th>
                        </tr>
                      </thead>
                      <tbody>
                        {datos.pendientes.map((solicitud) => (
                          <tr key={solicitud.idSolicitud}>
                            <td>
                              <div className="celda-doble">
                                <strong>{solicitud.plataforma}</strong>
                                <small>{solicitud.idSolicitud}</small>
                              </div>
                            </td>
                            <td>{solicitud.codigoApartamento}</td>
                            <td className="sin-corte">{formatearRango(solicitud.ingreso, solicitud.salida)}</td>
                            <td>{solicitud.nombreTitular}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </Bisel>
          </Revelar>

          <Revelar className="c5" retraso={120}>
            <Bisel>
              <div className="pila">
                <h2 className="titulo-seccion">
                  Procesadas <span className="tenue numero">({datos.resultados.length})</span>
                </h2>
                {datos.resultados.length === 0 && <p className="tenue">Aún no se ha procesado ninguna.</p>}
                <ul className="lista">
                  {datos.resultados.slice(-8).reverse().map((resultado) => (
                    <li key={resultado.idSolicitud} className="fila-lista fila-lista--fija">
                      <div className="celda-doble">
                        <strong>
                          {resultado.plataforma} · {resultado.codigoApartamento}
                        </strong>
                        <small>{resultado.aceptada ? `Reserva ${resultado.idReserva}` : resultado.motivo}</small>
                      </div>
                      <Insignia tono={resultado.aceptada ? 'salvia' : 'rojo'}>{resultado.aceptada ? 'Aceptada' : 'Rechazada'}</Insignia>
                    </li>
                  ))}
                </ul>
              </div>
            </Bisel>
          </Revelar>

          <Revelar className="c12">
            <Bisel>
              <div className="pila">
                <h2 className="titulo-seccion">
                  Conflictos <em>detectados</em> <span className="tenue numero">({datos.conflictos.length})</span>
                </h2>
                {datos.conflictos.length === 0 ? (
                  <p className="tenue">Sin conflictos: ninguna solicitud ha chocado con reservas existentes.</p>
                ) : (
                  <div className="tabla-contenedor">
                    <table className="tabla">
                      <thead>
                        <tr>
                          <th>Solicitud</th>
                          <th>Apartamento</th>
                          <th>Estancia</th>
                          <th>Choca con</th>
                          <th>Motivo</th>
                        </tr>
                      </thead>
                      <tbody>
                        {datos.conflictos.map((conflicto) => (
                          <tr key={conflicto.idSolicitud}>
                            <td>
                              {conflicto.plataforma} · {conflicto.idSolicitud}
                            </td>
                            <td>{conflicto.codigoApartamento}</td>
                            <td className="sin-corte">{formatearRango(conflicto.ingreso, conflicto.salida)}</td>
                            <td>
                              <Insignia tono="rojo">{conflicto.idReservaEnConflicto}</Insignia>
                            </td>
                            <td className="tenue">{conflicto.motivo}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </Bisel>
          </Revelar>
        </div>
      )}

      <Modal abierto={simulando} alCerrar={() => setSimulando(false)} eyebrow="Plataforma simulada" titulo="Nueva solicitud externa" ancho={720}>
        <FormularioSolicitud
          alTerminar={() => {
            setSimulando(false);
            recargar();
          }}
        />
      </Modal>
    </div>
  );
}
