import { ArrowRight, Broom, CheckCircle, Path, Play, Warning } from '@phosphor-icons/react';
import { useState } from 'react';
import { Bisel } from '../componentes/Bisel.jsx';
import { Boton } from '../componentes/Boton.jsx';
import { Encabezado } from '../componentes/Encabezado.jsx';
import { Cargando, Vacio } from '../componentes/Estados.jsx';
import { Insignia } from '../componentes/Insignia.jsx';
import { Revelar } from '../componentes/Revelar.jsx';
import { useAvisos } from '../contexto/Avisos.jsx';
import { useReloj } from '../contexto/Reloj.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { operacionesApi } from '../servicios/api.js';
import { formatearHora } from '../utilidades/fechas.js';

/** Cola de preparación (cola de prioridad), apartamentos en curso y la ruta de aseo más corta. */
export default function Preparacion() {
  const avisos = useAvisos();
  const { estado: reloj } = useReloj();
  const [ocupado, setOcupado] = useState(false);

  const { datos, cargando, recargar } = useConsulta(async () => {
    const [cola, enCurso, ruta] = await Promise.all([operacionesApi.colaPreparacion(), operacionesApi.enPreparacion(), operacionesApi.rutaPreparacion().catch(() => null)]);
    return { cola, enCurso, ruta };
  });

  async function ejecutar(accion, mensaje) {
    setOcupado(true);
    try {
      await accion();
      avisos.exito(mensaje);
      recargar();
    } catch {
      // aviso ya mostrado por el cliente de la API
    } finally {
      setOcupado(false);
    }
  }

  const minutos = (inicio) => (reloj ? Math.max(0, Math.round((new Date(reloj.ahora) - new Date(inicio)) / 60000)) : 0);

  return (
    <div className="pagina">
      <Encabezado eyebrow="Operación" titulo={<>Cola de <em>preparación</em></>} descripcion="Los apartamentos con llegadas más próximas salen primero. Inicia el siguiente cuando haya personal libre.">
        <Boton isla={Play} disabled={ocupado || !datos?.cola.length} onClick={() => ejecutar(operacionesApi.iniciarSiguientePreparacion, 'Preparación iniciada')}>
          Iniciar siguiente
        </Boton>
      </Encabezado>

      {cargando && <Cargando bloques={3} />}

      {datos && (
        <div className="bento">
          <Revelar className="c7">
            <Bisel>
              <div className="pila">
                <div className="fila separado">
                  <h2 className="titulo-seccion">
                    En <em>espera</em>
                  </h2>
                  <span className="tenue numero">{datos.cola.length} apartamentos</span>
                </div>
                {datos.cola.length === 0 && <Vacio icono={Broom} titulo="Cola vacía" texto="No hay apartamentos pendientes de preparación." />}
                <ol className="cola">
                  {datos.cola.map((tarea) => (
                    <li key={tarea.codigoApartamento}>
                      <span className="cola__posicion">{tarea.posicion}</span>
                      <div className="celda-doble" style={{ flex: 1 }}>
                        <strong>{tarea.nombreApartamento}</strong>
                        <small>
                          {tarea.codigoApartamento} · bloque {tarea.bloque}
                        </small>
                      </div>
                      {tarea.llegadaHoy ? <Insignia tono="ambar">Llega hoy {formatearHora(tarea.proximaLlegada)}</Insignia> : <Insignia>Prioridad {tarea.prioridad}</Insignia>}
                    </li>
                  ))}
                </ol>
              </div>
            </Bisel>
          </Revelar>

          <Revelar className="c5" retraso={120}>
            <Bisel oscuro>
              <div className="pila">
                <h2 className="titulo-seccion">
                  En <em style={{ color: 'var(--salvia-suave)' }}>curso</em>
                </h2>
                {datos.enCurso.length === 0 && <p style={{ opacity: 0.65 }}>Nadie está preparando un apartamento ahora mismo.</p>}
                {datos.enCurso.map((tarea) => (
                  <div key={tarea.codigoApartamento} className="fila separado" style={{ padding: '0.9rem 0', borderTop: '1px solid rgb(255 255 255 / 0.12)' }}>
                    <div className="celda-doble">
                      <strong>{tarea.nombreApartamento}</strong>
                      <small style={{ color: 'rgb(251 248 242 / 0.6)' }}>{minutos(tarea.fechaInicio)} min trabajando</small>
                    </div>
                    <Boton variante="salvia" pequeno icono={CheckCircle} disabled={ocupado} onClick={() => ejecutar(() => operacionesApi.finalizarPreparacion(tarea.codigoApartamento), `${tarea.nombreApartamento} listo`)}>
                      Terminar
                    </Boton>
                  </div>
                ))}
              </div>
            </Bisel>
          </Revelar>

          <Revelar className="c12">
            <Bisel>
              <div className="pila" style={{ gap: '1.25rem' }}>
                <div className="fila separado">
                  <h2 className="titulo-seccion">
                    Ruta de <em>aseo</em>
                  </h2>
                  {datos.ruta && (
                    <span className="eyebrow">
                      <Path weight="light" size={12} /> {Math.round(datos.ruta.metrosTotales)} m en total
                    </span>
                  )}
                </div>
                {!datos.ruta || datos.ruta.paradas.length === 0 ? (
                  <p className="tenue">No hay paradas pendientes: la ruta aparece cuando hay apartamentos en la cola.</p>
                ) : (
                  <>
                    <p className="tenue">Recorrido sugerido con el camino más corto entre paradas (Dijkstra), partiendo de {datos.ruta.inicio}.</p>
                    <ol className="ruta">
                      <li>
                        <span className="ruta__nodo ruta__nodo--inicio">{datos.ruta.inicio}</span>
                      </li>
                      {datos.ruta.tramos.map((tramo) => (
                        <li key={`${tramo.desde}-${tramo.hasta}`}>
                          <span className="ruta__tramo">
                            <ArrowRight weight="light" /> {Math.round(tramo.metros)} m
                          </span>
                          <span className="ruta__nodo">{tramo.hasta}</span>
                        </li>
                      ))}
                    </ol>
                  </>
                )}
                {datos.ruta?.inalcanzables.length > 0 && (
                  <div className="nota nota--aviso">
                    <Warning weight="light" />
                    <span>Sin camino abierto hasta: {datos.ruta.inalcanzables.join(', ')}. Revisa los pasillos cerrados en el mapa.</span>
                  </div>
                )}
              </div>
            </Bisel>
          </Revelar>
        </div>
      )}
    </div>
  );
}
