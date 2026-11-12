import { ArrowLeft, ArrowRight } from '@phosphor-icons/react';
import { useState } from 'react';
import { Bisel } from '../componentes/Bisel.jsx';
import { Boton } from '../componentes/Boton.jsx';
import { DetalleReserva } from '../componentes/DetalleReserva.jsx';
import { Encabezado } from '../componentes/Encabezado.jsx';
import { Cargando } from '../componentes/Estados.jsx';
import { Revelar } from '../componentes/Revelar.jsx';
import { useReloj } from '../contexto/Reloj.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { administracionApi } from '../servicios/api.js';
import { mesDe, nombreMes, sumarMeses } from '../utilidades/fechas.js';

/** Calendario de ocupación del mes: una fila por apartamento y una celda por noche. */
export default function Calendario() {
  const { hoy } = useReloj();
  const [mes, setMes] = useState(() => mesDe(hoy));
  const [abierta, setAbierta] = useState(null);
  const { datos, cargando, actualizando, recargar } = useConsulta(() => administracionApi.calendario(mes), [mes]);

  return (
    <div className="pagina">
      <Encabezado eyebrow="Ocupación" titulo={<>Calendario de <em>{nombreMes(mes)}</em></>} descripcion="Cada celda es una noche. Toca una noche ocupada para abrir su reserva.">
        <div className="fila">
          <Boton variante="suave" soloIcono aria-label="Mes anterior" onClick={() => setMes(sumarMeses(mes, -1))}>
            <ArrowLeft weight="light" size={18} />
          </Boton>
          <Boton variante="suave" onClick={() => setMes(mesDe(hoy))}>
            Hoy
          </Boton>
          <Boton variante="suave" soloIcono aria-label="Mes siguiente" onClick={() => setMes(sumarMeses(mes, 1))}>
            <ArrowRight weight="light" size={18} />
          </Boton>
        </div>
      </Encabezado>

      <Revelar>
        <Bisel>
          {cargando && <Cargando bloques={4} />}
          {datos && (
            <>
              <div className="leyenda">
                <span className="leyenda__item">
                  <i className="celda--LIBRE" /> Libre
                </span>
                <span className="leyenda__item">
                  <i className="celda--OCUPADO" /> Reservada
                </span>
                <span className="leyenda__item">
                  <i className="celda--BLOQUEADA" /> Bloqueada (fuera de servicio)
                </span>
              </div>
              <div className="calendario" style={{ opacity: actualizando ? 0.5 : 1 }}>
                <table>
                  <thead>
                    <tr>
                      <th className="calendario__nombre" />
                      {datos.dias.map((dia) => (
                        <th key={dia} className={dia === hoy ? 'calendario__hoy' : ''}>
                          {Number(dia.slice(8))}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {datos.filas.map((fila) => (
                      <tr key={fila.codigoApartamento}>
                        <th className="calendario__nombre" scope="row">
                          <span>{fila.nombreApartamento}</span>
                          <small>{fila.codigoApartamento}</small>
                        </th>
                        {fila.celdas.map((celda) => (
                          <td key={celda.noche} className={celda.noche === hoy ? 'calendario__hoy' : ''}>
                            <button type="button" className={`celda celda--${celda.estado}`} disabled={!celda.idReserva} title={celda.detalle ?? celda.estado.toLowerCase()} aria-label={`${fila.nombreApartamento}, ${celda.noche}: ${celda.estado.toLowerCase()}`} onClick={() => celda.idReserva && setAbierta(celda.idReserva)} />
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </Bisel>
      </Revelar>

      <DetalleReserva id={abierta} alCerrar={() => setAbierta(null)} alCambiar={recargar} />
    </div>
  );
}
