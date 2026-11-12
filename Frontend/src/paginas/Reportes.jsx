import { useState } from 'react';
import { Bisel } from '../componentes/Bisel.jsx';
import { Boton } from '../componentes/Boton.jsx';
import { Encabezado } from '../componentes/Encabezado.jsx';
import { Cargando } from '../componentes/Estados.jsx';
import { Insignia } from '../componentes/Insignia.jsx';
import { Revelar } from '../componentes/Revelar.jsx';
import { useReloj } from '../contexto/Reloj.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { administracionApi } from '../servicios/api.js';
import { formatearDinero, formatearDineroCorto } from '../utilidades/dinero.js';
import { TONO_TEMPORADA } from '../utilidades/estados.js';
import { sumarDias } from '../utilidades/fechas.js';

/** Barra horizontal: el relleno se escala con `transform` (`valor` entre 0 y 1). */
function BarraHorizontal({ etiqueta, detalle, valor, tono }) {
  return (
    <div className="barra-fila">
      <div className="fila separado pequeno">
        <span>{etiqueta}</span>
        <span className="numero tenue">{detalle}</span>
      </div>
      <div className="barra">
        <div className={`barra__relleno ${tono ? `barra__relleno--${tono}` : ''}`} style={{ '--valor': Math.min(1, Math.max(0, valor)) }} />
      </div>
    </div>
  );
}

/** Reportes del administrador: ingresos, ocupación por temporada y apartamento, canales y operación. */
export default function Reportes() {
  const { hoy } = useReloj();
  const [edicion, setEdicion] = useState({});
  const [aplicado, setAplicado] = useState(null);
  const porDefecto = { desde: sumarDias(hoy, -90), hasta: sumarDias(hoy, 90) };
  const valores = { ...porDefecto, ...edicion };
  const rango = aplicado ?? porDefecto;

  const { datos: reporte, cargando, actualizando } = useConsulta(() => administracionApi.reporte(rango.desde, rango.hasta), [rango.desde, rango.hasta]);

  const maxIngreso = Math.max(1, ...(reporte?.ingresosPorMes ?? []).map((m) => m.ingresos));
  const maxTemporada = Math.max(1, ...(reporte?.ocupacionPorTemporada ?? []).map((t) => t.tarifa));
  const maxOcupacion = Math.max(1, ...(reporte?.ocupacionPorApartamento ?? []).map((a) => a.tarifa));

  return (
    <div className="pagina">
      <Encabezado eyebrow="Administración" titulo={<>Reportes y <em>estadísticas</em></>} descripcion="Ingresos, ocupación y operación del periodo que elijas." />

      <Revelar>
        <Bisel compacto>
          <form
            className="formulario"
            onSubmit={(evento) => {
              evento.preventDefault();
              setAplicado(valores);
            }}
          >
            <label className="campo">
              <span>Desde</span>
              <input type="date" value={valores.desde} onChange={(e) => setEdicion((p) => ({ ...p, desde: e.target.value }))} />
            </label>
            <label className="campo">
              <span>Hasta</span>
              <input type="date" value={valores.hasta} min={valores.desde} onChange={(e) => setEdicion((p) => ({ ...p, hasta: e.target.value }))} />
            </label>
            <Boton type="submit" disabled={valores.hasta < valores.desde}>
              Actualizar
            </Boton>
          </form>
        </Bisel>
      </Revelar>

      {cargando && <Cargando bloques={4} />}

      {reporte && (
        <div className="bento" style={{ opacity: actualizando ? 0.5 : 1, transition: 'opacity 0.6s var(--muelle)' }}>
          <Revelar className="c5">
            <Bisel oscuro>
              <span className="eyebrow" style={{ color: 'var(--crema)', background: 'rgb(255 255 255 / 0.1)' }}>
                Ingresos totales
              </span>
              <p className="cifra" style={{ marginTop: '1.2rem', fontSize: 'clamp(2.2rem, 4vw, 3.6rem)' }}>
                {formatearDinero(reporte.ingresosTotales)}
              </p>
              <p className="pequeno" style={{ opacity: 0.65, marginTop: '0.5rem' }}>
                del {reporte.desde} al {reporte.hasta}
              </p>
            </Bisel>
          </Revelar>

          <Revelar className="c7" retraso={100}>
            <Bisel>
              <div className="rejilla" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(7.5rem, 1fr))' }}>
                {[
                  [reporte.preparacionesTerminadas, 'preparaciones terminadas'],
                  [`${reporte.minutosPromedioPreparacion.toFixed(0)} min`, 'promedio por preparación'],
                  [reporte.preparacionesEnCola, 'en cola ahora'],
                  [reporte.conflictosCanal, 'conflictos del canal externo'],
                ].map(([valor, texto]) => (
                  <div key={texto} className="pila" style={{ gap: '0.4rem' }}>
                    <p className="cifra">{valor}</p>
                    <p className="pequeno tenue">{texto}</p>
                  </div>
                ))}
              </div>
            </Bisel>
          </Revelar>

          <Revelar className="c7">
            <Bisel>
              <div className="pila" style={{ gap: '1.5rem' }}>
                <h2 className="titulo-seccion">
                  Ingresos <em>por mes</em>
                </h2>
                {reporte.ingresosPorMes.length === 0 ? (
                  <p className="tenue">Sin ingresos en el periodo.</p>
                ) : (
                  <div className="columnas">
                    {reporte.ingresosPorMes.map((mes) => (
                      <div key={mes.periodo} className="columna">
                        <span className="numero pequeno" title={formatearDinero(mes.ingresos)}>{formatearDineroCorto(mes.ingresos)}</span>
                        <div className="columna__riel">
                          <div className="columna__barra" style={{ '--valor': mes.ingresos / maxIngreso }} />
                        </div>
                        <span className="pequeno tenue">{mes.periodo}</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </Bisel>
          </Revelar>

          <Revelar className="c5" retraso={100}>
            <Bisel>
              <div className="pila" style={{ gap: '1.1rem' }}>
                <h2 className="titulo-seccion">
                  Por <em>temporada</em>
                </h2>
                {reporte.ocupacionPorTemporada.map((t) => (
                  <div key={t.temporada} className="pila" style={{ gap: '0.4rem' }}>
                    <div className="fila separado">
                      <Insignia tono={TONO_TEMPORADA[t.temporada]}>{t.etiquetaTemporada}</Insignia>
                      <span className="numero tenue pequeno">
                        {t.nochesOcupadas} / {t.nochesDisponibles} noches
                      </span>
                    </div>
                    <BarraHorizontal etiqueta="Ocupación" detalle={`${t.tarifa.toFixed(1)}%`} valor={t.tarifa / maxTemporada} tono="ambar" />
                  </div>
                ))}
              </div>
            </Bisel>
          </Revelar>

          <Revelar className="c6">
            <Bisel>
              <div className="pila" style={{ gap: '1.1rem' }}>
                <h2 className="titulo-seccion">
                  Ocupación por <em>apartamento</em>
                </h2>
                {reporte.ocupacionPorApartamento.map((a) => (
                  <BarraHorizontal key={a.codigo} etiqueta={`${a.nombre} · ${a.codigo}`} detalle={`${a.tarifa.toFixed(1)}% · ${a.nochesOcupadas} noches`} valor={a.tarifa / maxOcupacion} />
                ))}
              </div>
            </Bisel>
          </Revelar>

          <Revelar className="c6" retraso={100}>
            <Bisel>
              <div className="pila">
                <h2 className="titulo-seccion">
                  Reservas por <em>canal</em>
                </h2>
                <div className="tabla-contenedor">
                  <table className="tabla">
                    <thead>
                      <tr>
                        <th>Canal</th>
                        <th className="derecha">Total</th>
                        <th className="derecha">Conf.</th>
                        <th className="derecha">Curso</th>
                        <th className="derecha">Fin.</th>
                        <th className="derecha">Canc.</th>
                      </tr>
                    </thead>
                    <tbody>
                      {reporte.reservasPorCanal.map((c) => (
                        <tr key={c.canal}>
                          <td>{c.etiquetaCanal}</td>
                          <td className="derecha numero">{c.total}</td>
                          <td className="derecha numero">{c.confirmada}</td>
                          <td className="derecha numero">{c.enCurso}</td>
                          <td className="derecha numero">{c.finalizada}</td>
                          <td className="derecha numero">{c.cancelada + c.noPresentado}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </Bisel>
          </Revelar>
        </div>
      )}
    </div>
  );
}
