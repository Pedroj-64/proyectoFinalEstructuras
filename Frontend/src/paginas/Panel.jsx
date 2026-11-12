import { ArrowUpRight, Broom, CalendarCheck, Clock, Plugs, SignIn, SignOut } from '@phosphor-icons/react';
import { useState } from 'react';
import { Bisel } from '../componentes/Bisel.jsx';
import { Boton } from '../componentes/Boton.jsx';
import { DetalleReserva } from '../componentes/DetalleReserva.jsx';
import { Encabezado } from '../componentes/Encabezado.jsx';
import { Cargando, Vacio } from '../componentes/Estados.jsx';
import { Insignia } from '../componentes/Insignia.jsx';
import { Revelar } from '../componentes/Revelar.jsx';
import { useReloj } from '../contexto/Reloj.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { useNavegacion } from '../navegacion/Navegacion.jsx';
import { apartamentosApi, operacionesApi, reservasApi } from '../servicios/api.js';
import { ETIQUETA_APARTAMENTO, TONO_RESERVA } from '../utilidades/estados.js';
import { formatearHora, formatearFechaHora, saludo, tiempoRestante } from '../utilidades/fechas.js';

/** Colores de cada tramo de la barra "estado del edificio". */
const COLOR_ESTADO = {
  PREPARADO: 'var(--salvia)',
  OCUPADO: 'var(--pizarra)',
  PENDIENTE_PREPARACION: 'var(--ambar)',
  EN_PREPARACION: '#d1a050',
  FUERA_DE_SERVICIO: 'var(--terracota)',
};

/** Fila clicable de reserva dentro de una lista del panel. */
function FilaReserva({ reserva, detalle, alAbrir }) {
  return (
    <li>
      <button type="button" className="fila-lista" onClick={() => alAbrir(reserva.id)}>
        <div className="celda-doble">
          <strong>{reserva.nombreTitular}</strong>
          <small>
            {reserva.nombreApartamento} · {detalle}
          </small>
        </div>
        <Insignia tono={TONO_RESERVA[reserva.estado]}>{reserva.etiquetaEstado}</Insignia>
      </button>
    </li>
  );
}

/** Panel del personal: pulso del día (llegadas, salidas, pendientes, estado del edificio y turno). */
export default function Panel() {
  const { estado: reloj, hoy } = useReloj();
  const { irRaiz } = useNavegacion();
  const [abierta, setAbierta] = useState(null);

  const { datos, cargando, recargar } = useConsulta(async () => {
    const [apartamentos, reservas, turno, turnos, cola, conflictos] = await Promise.all([
      apartamentosApi.listar(),
      reservasApi.listar(),
      operacionesApi.turnoActual().catch(() => null),
      operacionesApi.turnosProximos(3).catch(() => []),
      operacionesApi.colaPreparacion(),
      operacionesApi.conflictosCanal(),
    ]);
    return { apartamentos, reservas, turno, turnos, cola, conflictos };
  }, [hoy]);

  const hora = reloj ? new Date(reloj.ahora).getHours() : new Date().getHours();

  const llegadas = (datos?.reservas ?? []).filter((r) => r.ingreso === hoy && (r.estado === 'CONFIRMADA' || r.estado === 'PENDIENTE'));
  const salidas = (datos?.reservas ?? []).filter((r) => r.salida === hoy && r.estado === 'EN_CURSO');
  const porVencer = (datos?.reservas ?? []).filter((r) => r.estado === 'PENDIENTE').sort((a, b) => a.fechaVencimiento.localeCompare(b.fechaVencimiento));

  const porEstado = {};
  for (const apartamento of datos?.apartamentos ?? []) porEstado[apartamento.estado] = (porEstado[apartamento.estado] ?? 0) + 1;
  const total = datos?.apartamentos.length ?? 0;
  const ocupados = porEstado.OCUPADO ?? 0;

  return (
    <div className="pagina">
      <Encabezado eyebrow={reloj?.simulado ? 'Reloj simulado' : 'En vivo'} titulo={<>{saludo(hora)}, <em>equipo</em></>} descripcion={reloj ? `Hoy es ${formatearFechaHora(reloj.ahora)}` : undefined}>
        <Boton isla={ArrowUpRight} onClick={() => irRaiz('reservas')}>
          Ver todas las reservas
        </Boton>
      </Encabezado>

      {cargando && <Cargando bloques={4} />}

      {datos && (
        <>
          <div className="bento">
            <Revelar className="c8">
              <Bisel>
                <div className="fila separado" style={{ alignItems: 'flex-start' }}>
                  <div className="pila">
                    <span className="eyebrow">Recepción de turno</span>
                    {datos.turno ? (
                      <>
                        <h2 className="titulo-seccion">
                          {datos.turno.nombreRecepcionista} <em>· turno {datos.turno.etiquetaFranja.toLowerCase()}</em>
                        </h2>
                        <p className="tenue">
                          <Clock weight="light" style={{ verticalAlign: '-3px' }} /> {formatearHora(datos.turno.inicio)} a {formatearHora(datos.turno.fin)}
                        </p>
                      </>
                    ) : (
                      <h2 className="titulo-seccion">Sin turno asignado</h2>
                    )}
                  </div>
                  <div className="pila" style={{ gap: '0.4rem', textAlign: 'right' }}>
                    <span className="pequeno tenue">Siguientes</span>
                    {datos.turnos.slice(0, 3).map((turno) => (
                      <span key={turno.inicio} className="pequeno">
                        {turno.etiquetaFranja} · {turno.nombreRecepcionista.split(' ')[0]}
                      </span>
                    ))}
                  </div>
                </div>
              </Bisel>
            </Revelar>

            <Revelar className="c4" retraso={100}>
              <Bisel oscuro>
                <span className="eyebrow" style={{ color: 'var(--crema)', background: 'rgb(255 255 255 / 0.1)' }}>
                  Ocupación
                </span>
                <p className="cifra" style={{ marginTop: '1.2rem' }}>
                  {total ? Math.round((ocupados / total) * 100) : 0}%
                </p>
                <p className="pequeno" style={{ opacity: 0.7, marginTop: '0.5rem' }}>
                  {ocupados} de {total} apartamentos ocupados
                </p>
              </Bisel>
            </Revelar>

            <Revelar className="c4">
              <Bisel className="tarjeta-lista">
                <div className="fila separado">
                  <h2 className="titulo-tarjeta">Llegadas de hoy</h2>
                  <SignIn weight="light" size={22} className="tenue" />
                </div>
                <p className="cifra">{llegadas.length}</p>
                {llegadas.length === 0 ? (
                  <p className="tenue pequeno">Nadie llega hoy.</p>
                ) : (
                  <ul className="lista">
                    {llegadas.map((r) => (
                      <FilaReserva key={r.id} reserva={r} detalle={`llega ${formatearHora(r.llegadaEstimada)}`} alAbrir={setAbierta} />
                    ))}
                  </ul>
                )}
              </Bisel>
            </Revelar>

            <Revelar className="c4" retraso={100}>
              <Bisel className="tarjeta-lista">
                <div className="fila separado">
                  <h2 className="titulo-tarjeta">Salidas de hoy</h2>
                  <SignOut weight="light" size={22} className="tenue" />
                </div>
                <p className="cifra">{salidas.length}</p>
                {salidas.length === 0 ? (
                  <p className="tenue pequeno">Ninguna salida pendiente.</p>
                ) : (
                  <ul className="lista">
                    {salidas.map((r) => (
                      <FilaReserva key={r.id} reserva={r} detalle={r.codigoApartamento} alAbrir={setAbierta} />
                    ))}
                  </ul>
                )}
              </Bisel>
            </Revelar>

            <Revelar className="c4" retraso={200}>
              <Bisel className="tarjeta-lista">
                <div className="fila separado">
                  <h2 className="titulo-tarjeta">Pendientes de anticipo</h2>
                  <CalendarCheck weight="light" size={22} className="tenue" />
                </div>
                <p className="cifra">{porVencer.length}</p>
                {porVencer.length === 0 ? (
                  <p className="tenue pequeno">Todo al día.</p>
                ) : (
                  <ul className="lista">
                    {porVencer.slice(0, 4).map((r) => (
                      <FilaReserva key={r.id} reserva={r} detalle={`vence en ${reloj ? tiempoRestante(r.fechaVencimiento, reloj.ahora) : '—'}`} alAbrir={setAbierta} />
                    ))}
                  </ul>
                )}
              </Bisel>
            </Revelar>

            <Revelar className="c8">
              <Bisel>
                <div className="pila" style={{ gap: '1.5rem' }}>
                  <div className="fila separado">
                    <h2 className="titulo-seccion">
                      Estado del <em>edificio</em>
                    </h2>
                    <Boton variante="suave" pequeno onClick={() => irRaiz('apartamentos')}>
                      Ver apartamentos
                    </Boton>
                  </div>
                  <div className="segmentada" role="img" aria-label="Distribución de estados de los apartamentos">
                    {Object.entries(porEstado).map(([estado, cantidad]) => (
                      <span key={estado} style={{ flexGrow: cantidad, background: COLOR_ESTADO[estado] }} title={`${ETIQUETA_APARTAMENTO[estado]}: ${cantidad}`} />
                    ))}
                  </div>
                  <div className="leyenda">
                    {Object.entries(porEstado).map(([estado, cantidad]) => (
                      <span key={estado} className="leyenda__item">
                        <i style={{ background: COLOR_ESTADO[estado] }} />
                        {ETIQUETA_APARTAMENTO[estado]} <b className="numero">{cantidad}</b>
                      </span>
                    ))}
                  </div>
                </div>
              </Bisel>
            </Revelar>

            <Revelar className="c4" retraso={100}>
              <Bisel>
                <div className="pila" style={{ gap: '0.9rem' }}>
                  <h2 className="titulo-tarjeta">Operación</h2>
                  <button type="button" className="atajo" onClick={() => irRaiz('preparacion')}>
                    <Broom weight="light" size={22} />
                    <span>
                      <strong className="numero">{datos.cola.length}</strong> apartamentos en la cola de preparación
                    </span>
                    <ArrowUpRight weight="light" />
                  </button>
                  <button type="button" className="atajo" onClick={() => irRaiz('canal')}>
                    <Plugs weight="light" size={22} />
                    <span>
                      <strong className="numero">{datos.conflictos.length}</strong> conflictos en el canal externo
                    </span>
                    <ArrowUpRight weight="light" />
                  </button>
                </div>
              </Bisel>
            </Revelar>
          </div>
          {llegadas.length + salidas.length + porVencer.length === 0 && <Vacio titulo="Día tranquilo" texto="No hay llegadas, salidas ni anticipos pendientes." />}
        </>
      )}

      <DetalleReserva id={abierta} alCerrar={() => setAbierta(null)} alCambiar={recargar} />
    </div>
  );
}
