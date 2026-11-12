import { LockKey, LockKeyOpen } from '@phosphor-icons/react';
import { useState } from 'react';
import { Bisel } from '../componentes/Bisel.jsx';
import { Boton } from '../componentes/Boton.jsx';
import { Encabezado } from '../componentes/Encabezado.jsx';
import { Cargando } from '../componentes/Estados.jsx';
import { Insignia } from '../componentes/Insignia.jsx';
import { Modal } from '../componentes/Modal.jsx';
import { Revelar } from '../componentes/Revelar.jsx';
import { useAvisos } from '../contexto/Avisos.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { operacionesApi } from '../servicios/api.js';

/** Color de cada nodo según su tipo o, si es un apartamento, según su estado operativo. */
function colorNodo(nodo) {
  if (nodo.tipo === 'RECEPCION') return 'var(--tinta)';
  if (nodo.tipo === 'ESCALERA') return 'var(--tinta-3)';
  if (nodo.tipo === 'CUARTO_ASEO') return 'var(--ambar)';
  return { PREPARADO: 'var(--salvia)', OCUPADO: 'var(--pizarra)', PENDIENTE_PREPARACION: 'var(--ambar)', EN_PREPARACION: '#d1a050', FUERA_DE_SERVICIO: 'var(--terracota)' }[nodo.estadoApartamento] ?? 'var(--tinta-3)';
}

const llave = (a, b) => [a, b].sort().join('|');

/** Contenido del modal de un pasillo: muestra el impacto de cerrarlo y permite abrirlo o cerrarlo. */
function ContenidoPasillo({ pasillo, alTerminar }) {
  const avisos = useAvisos();
  const { datos: impacto } = useConsulta(() => (pasillo.abierto ? operacionesApi.impactoCierre(pasillo.desde, pasillo.hasta) : Promise.resolve([])), [pasillo.desde, pasillo.hasta]);

  async function alternar() {
    try {
      await operacionesApi.establecerPasilloAbierto(pasillo.desde, pasillo.hasta, !pasillo.abierto);
      avisos.exito(pasillo.abierto ? 'Pasillo cerrado' : 'Pasillo abierto');
      alTerminar();
    } catch {
      // aviso ya mostrado
    }
  }

  return (
    <>
      <div className="fila">
        <Insignia tono={pasillo.abierto ? 'salvia' : 'rojo'}>{pasillo.abierto ? 'Abierto' : 'Cerrado'}</Insignia>
        <Insignia>{pasillo.metros} m</Insignia>
      </div>
      {pasillo.abierto &&
        (impacto === undefined ? (
          <Cargando bloques={1} />
        ) : impacto.length === 0 ? (
          <div className="nota nota--salvia">Cerrar este pasillo no deja ningún apartamento sin acceso.</div>
        ) : (
          <div className="nota nota--aviso">Si lo cierras, quedan sin camino desde recepción: {impacto.join(', ')}.</div>
        ))}
      <div className="encabezado__acciones" style={{ justifyContent: 'flex-end' }}>
        <Boton variante="suave" onClick={alTerminar}>
          Cancelar
        </Boton>
        <Boton variante={pasillo.abierto ? 'peligro' : 'salvia'} icono={pasillo.abierto ? LockKey : LockKeyOpen} onClick={alternar}>
          {pasillo.abierto ? 'Cerrar pasillo' : 'Abrir pasillo'}
        </Boton>
      </div>
    </>
  );
}

/**
 * Mapa del edificio dibujado como un grafo en SVG. Toca dos nodos para ver el camino más corto
 * entre ellos (Dijkstra, calculado por el backend) y toca un pasillo para abrirlo o cerrarlo.
 */
export default function MapaEdificio() {
  const [seleccion, setSeleccion] = useState([]); // hasta dos ids de nodos
  const [pasillo, setPasillo] = useState(null);

  const { datos: mapa, cargando, recargar } = useConsulta(operacionesApi.mapaEdificio);
  const { datos: analisis, recargar: recargarAnalisis } = useConsulta(() => operacionesApi.analisis('REC'));
  const [desde, hasta] = seleccion;
  const { datos: camino } = useConsulta(() => (desde && hasta ? operacionesApi.caminoMasCorto(desde, hasta) : Promise.resolve(null)), [desde, hasta, mapa]);

  const refrescar = () => {
    recargar();
    recargarAnalisis();
  };

  if (cargando || !mapa) {
    return (
      <div className="pagina">
        <Cargando bloques={3} />
      </div>
    );
  }

  const margen = 70;
  const xs = mapa.nodos.map((n) => n.x);
  const ys = mapa.nodos.map((n) => n.y);
  const caja = [Math.min(...xs) - margen, Math.min(...ys) - margen, Math.max(...xs) - Math.min(...xs) + margen * 2, Math.max(...ys) - Math.min(...ys) + margen * 2];
  const porId = Object.fromEntries(mapa.nodos.map((nodo) => [nodo.id, nodo]));

  const aristasCamino = new Set();
  camino?.camino.forEach((id, i) => i > 0 && aristasCamino.add(llave(camino.camino[i - 1], id)));

  const tocar = (id) => setSeleccion((actual) => (actual.length === 1 && actual[0] !== id ? [actual[0], id] : [id]));

  return (
    <div className="pagina">
      <Encabezado eyebrow="Edificio" titulo={<>Mapa de <em>pasillos</em></>} descripcion="Toca dos puntos para trazar la ruta más corta. Toca un pasillo para cerrarlo (por ejemplo, por mantenimiento) y ver a quién deja incomunicado." />

      <div className="bento">
        <Revelar className="c9">
          <Bisel sinRelleno>
            <svg className="mapa" viewBox={caja.join(' ')} role="img" aria-label="Grafo de nodos y pasillos del edificio">
              {mapa.pasillos.map((p) => {
                const a = porId[p.desde];
                const b = porId[p.hasta];
                if (!a || !b) return null;
                const enRuta = aristasCamino.has(llave(p.desde, p.hasta));
                return (
                  <g key={llave(p.desde, p.hasta)}>
                    <line x1={a.x} y1={a.y} x2={b.x} y2={b.y} className={`mapa__pasillo ${p.abierto ? '' : 'mapa__pasillo--cerrado'} ${enRuta ? 'mapa__pasillo--ruta' : ''}`} />
                    <line x1={a.x} y1={a.y} x2={b.x} y2={b.y} className="mapa__zona" onClick={() => setPasillo(p)}>
                      <title>{`${p.desde} ↔ ${p.hasta} · ${p.metros} m · ${p.abierto ? 'abierto' : 'cerrado'}`}</title>
                    </line>
                  </g>
                );
              })}
              {mapa.nodos.map((nodo) => {
                const elegido = seleccion.includes(nodo.id);
                const enRuta = camino?.camino.includes(nodo.id);
                return (
                  <g key={nodo.id} className={`mapa__nodo ${elegido ? 'mapa__nodo--elegido' : ''}`} onClick={() => tocar(nodo.id)} tabIndex={0} role="button" aria-label={`${nodo.etiqueta}`} onKeyDown={(e) => e.key === 'Enter' && tocar(nodo.id)}>
                    <circle cx={nodo.x} cy={nodo.y} r={elegido || enRuta ? 22 : 17} fill={colorNodo(nodo)} />
                    <text x={nodo.x} y={nodo.y + 38} textAnchor="middle">
                      {nodo.tipo === 'APARTAMENTO' ? nodo.id : nodo.etiqueta}
                    </text>
                  </g>
                );
              })}
            </svg>
          </Bisel>
        </Revelar>

        <Revelar className="c3" retraso={120}>
          <Bisel>
            <div className="pila">
              <span className="eyebrow">Ruta</span>
              {seleccion.length === 0 && <p className="tenue">Elige un punto de partida.</p>}
              {seleccion.length === 1 && <p>Desde <strong>{porId[desde]?.etiqueta}</strong>. Elige el destino.</p>}
              {camino && (
                <>
                  <p>
                    <strong>{porId[desde]?.etiqueta}</strong> → <strong>{porId[hasta]?.etiqueta}</strong>
                  </p>
                  {camino.alcanzable ? (
                    <>
                      <p className="cifra">{Math.round(camino.metros)} m</p>
                      <p className="pequeno tenue">{camino.camino.join(' → ')}</p>
                    </>
                  ) : (
                    <div className="nota nota--peligro">No hay camino abierto entre esos dos puntos.</div>
                  )}
                </>
              )}
              {seleccion.length > 0 && (
                <Boton variante="suave" pequeno onClick={() => setSeleccion([])}>
                  Limpiar
                </Boton>
              )}
              <div className="leyenda leyenda--columna">
                {[
                  ['var(--salvia)', 'Apartamento preparado'],
                  ['var(--pizarra)', 'Apartamento ocupado'],
                  ['var(--ambar)', 'Por preparar / aseo'],
                  ['var(--terracota)', 'Fuera de servicio'],
                  ['var(--tinta)', 'Recepción'],
                ].map(([color, texto]) => (
                  <span key={texto} className="leyenda__item">
                    <i style={{ background: color }} />
                    {texto}
                  </span>
                ))}
              </div>
            </div>
          </Bisel>
        </Revelar>

        {analisis && (
          <Revelar className="c12">
            <Bisel>
              <div className="pila" style={{ gap: '1.25rem' }}>
                <h2 className="titulo-seccion">
                  Análisis del <em>grafo</em>
                </h2>
                <div className="rejilla">
                  <div className="pila" style={{ gap: '0.4rem' }}>
                    <span className="eyebrow">Componentes conexos</span>
                    <p className="cifra">{analisis.componentes.length}</p>
                    <p className="pequeno tenue">{analisis.componentes.length === 1 ? 'Todo el edificio está conectado.' : 'El edificio está partido en zonas aisladas.'}</p>
                  </div>
                  <div className="pila" style={{ gap: '0.4rem' }}>
                    <span className="eyebrow">Sin acceso desde recepción</span>
                    <p className="cifra">{analisis.apartamentosInalcanzables.length}</p>
                    <p className="pequeno tenue">{analisis.apartamentosInalcanzables.join(', ') || 'Ninguno'}</p>
                  </div>
                  <div className="pila" style={{ gap: '0.4rem' }}>
                    <span className="eyebrow">Recorrido en anchura (BFS)</span>
                    <p className="pequeno">{analisis.ordenAnchuraPrimero.join(' · ')}</p>
                  </div>
                  <div className="pila" style={{ gap: '0.4rem' }}>
                    <span className="eyebrow">Recorrido en profundidad (DFS)</span>
                    <p className="pequeno">{analisis.ordenProfundidadPrimero.join(' · ')}</p>
                  </div>
                </div>
              </div>
            </Bisel>
          </Revelar>
        )}
      </div>

      <Modal abierto={Boolean(pasillo)} alCerrar={() => setPasillo(null)} eyebrow="Pasillo" titulo={pasillo ? `${pasillo.desde} ↔ ${pasillo.hasta}` : ''} ancho={520}>
        {pasillo && (
          <ContenidoPasillo
            pasillo={pasillo}
            alTerminar={() => {
              setPasillo(null);
              refrescar();
            }}
          />
        )}
      </Modal>
    </div>
  );
}
