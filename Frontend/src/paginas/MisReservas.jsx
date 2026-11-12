import { Bed, Heart, Receipt } from '@phosphor-icons/react';
import { useState } from 'react';
import { Bisel } from '../componentes/Bisel.jsx';
import { BotonFavorito } from '../componentes/BotonFavorito.jsx';
import { Boton } from '../componentes/Boton.jsx';
import { DetalleReserva } from '../componentes/DetalleReserva.jsx';
import { Encabezado } from '../componentes/Encabezado.jsx';
import { Cargando, Vacio } from '../componentes/Estados.jsx';
import { ImagenFoto } from '../componentes/ImagenFoto.jsx';
import { Insignia } from '../componentes/Insignia.jsx';
import { Revelar } from '../componentes/Revelar.jsx';
import { useReloj } from '../contexto/Reloj.jsx';
import { useSesion } from '../contexto/Sesion.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { useNavegacion } from '../navegacion/Navegacion.jsx';
import { huespedesApi } from '../servicios/api.js';
import { formatearDinero } from '../utilidades/dinero.js';
import { TONO_RESERVA } from '../utilidades/estados.js';
import { formatearRango, tiempoRestante } from '../utilidades/fechas.js';
import { fotoApartamento } from '../utilidades/fotos.js';

/** Pantalla del huésped: sus reservas, su historial de estancias y sus apartamentos favoritos. */
export default function MisReservas() {
  const { documentoHuesped } = useSesion();
  const { estado: reloj } = useReloj();
  const { ir } = useNavegacion();
  const [pestana, setPestana] = useState('reservas');
  const [abierta, setAbierta] = useState(null);

  const { datos, cargando, recargar } = useConsulta(async () => {
    const [huesped, reservas, historial, favoritos] = await Promise.all([
      huespedesApi.buscar(documentoHuesped),
      huespedesApi.reservas(documentoHuesped),
      huespedesApi.historial(documentoHuesped),
      huespedesApi.favoritos(documentoHuesped),
    ]);
    return { huesped, reservas, historial, favoritos };
  }, [documentoHuesped]);

  const primerNombre = datos?.huesped.nombreCompleto.split(' ')[0] ?? '';
  const reservas = [...(datos?.reservas ?? [])].sort((a, b) => b.ingreso.localeCompare(a.ingreso));

  return (
    <div className="pagina">
      <Encabezado eyebrow="Mis estancias" titulo={primerNombre ? <>Hola, <em>{primerNombre}</em></> : 'Tus estancias'} descripcion="Tus reservas, lo que has pagado y los apartamentos que más te gustaron.">
        <Boton isla={Bed} onClick={() => ir('descubrir')}>
          Reservar otra estancia
        </Boton>
      </Encabezado>

      <div className="segmentos" role="tablist">
        {[
          ['reservas', `Reservas (${reservas.length})`],
          ['historial', 'Historial'],
          ['favoritos', `Favoritos (${datos?.favoritos.length ?? 0})`],
        ].map(([clave, etiqueta]) => (
          <button key={clave} type="button" role="tab" className="segmento" aria-selected={pestana === clave} onClick={() => setPestana(clave)}>
            {etiqueta}
          </button>
        ))}
      </div>

      {cargando && <Cargando bloques={3} />}

      {datos && pestana === 'reservas' && (
        <div className="rejilla">
          {reservas.length === 0 && <Vacio icono={Receipt} titulo="Aún no tienes reservas" texto="Cuando reserves, aparecerá aquí con su estado de pago." />}
          {reservas.map((reserva, indice) => (
            <Revelar key={reserva.id} retraso={indice * 80}>
              <Bisel sinRelleno interactivo className="tarjeta-reserva">
                <div className="tarjeta-reserva__foto">
                  <ImagenFoto src={fotoApartamento(reserva.codigoApartamento, 700)} alt={reserva.nombreApartamento} />
                  <Insignia tono={TONO_RESERVA[reserva.estado]}>{reserva.etiquetaEstado}</Insignia>
                </div>
                <div className="tarjeta-apto__cuerpo">
                  <div>
                    <h3 className="titulo-tarjeta">{reserva.nombreApartamento}</h3>
                    <p className="tenue pequeno">
                      {formatearRango(reserva.ingreso, reserva.salida)} · {reserva.noches} noches · {reserva.id}
                    </p>
                  </div>
                  <div className="pila" style={{ gap: '0.5rem' }}>
                    <div className="fila separado pequeno">
                      <span className="tenue">Pagado {formatearDinero(reserva.pagado)}</span>
                      <strong className="numero">{formatearDinero(reserva.total)}</strong>
                    </div>
                    <div className="barra">
                      <div className="barra__relleno" style={{ '--valor': reserva.total ? Math.min(1, reserva.pagado / reserva.total) : 0 }} />
                    </div>
                    {reserva.estado === 'PENDIENTE' && reloj && <p className="pequeno" style={{ color: 'var(--ambar)' }}>Paga el anticipo antes de {tiempoRestante(reserva.fechaVencimiento, reloj.ahora)} o se cancela.</p>}
                  </div>
                  <Boton variante="suave" pequeno onClick={() => setAbierta(reserva.id)}>
                    Ver detalle y folio
                  </Boton>
                </div>
              </Bisel>
            </Revelar>
          ))}
        </div>
      )}

      {datos && pestana === 'historial' && (
        <Bisel>
          {datos.historial.length === 0 ? (
            <Vacio icono={Receipt} titulo="Sin estancias completadas" texto="Tus estancias finalizadas se guardan aquí." />
          ) : (
            <div className="tabla-contenedor">
              <table className="tabla">
                <thead>
                  <tr>
                    <th>Apartamento</th>
                    <th>Fechas</th>
                    <th className="derecha">Noches</th>
                    <th className="derecha">Ocupantes</th>
                    <th className="derecha">Total</th>
                  </tr>
                </thead>
                <tbody>
                  {datos.historial.map((estancia) => (
                    <tr key={estancia.idReserva}>
                      <td>
                        <div className="celda-doble">
                          {estancia.nombreApartamento}
                          <small>{estancia.codigoApartamento}</small>
                        </div>
                      </td>
                      <td>{formatearRango(estancia.ingreso, estancia.salida)}</td>
                      <td className="derecha numero">{estancia.noches}</td>
                      <td className="derecha numero">{estancia.ocupantes}</td>
                      <td className="derecha numero">{formatearDinero(estancia.total)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Bisel>
      )}

      {datos && pestana === 'favoritos' && (
        <div className="rejilla">
          {datos.favoritos.length === 0 && <Vacio icono={Heart} titulo="Sin favoritos todavía" texto="Toca el corazón de un apartamento para guardarlo aquí." />}
          {datos.favoritos.map((apartamento, indice) => (
            <Revelar key={apartamento.codigo} retraso={indice * 80}>
              <Bisel sinRelleno interactivo className="tarjeta-reserva">
                <div className="tarjeta-reserva__foto">
                  <ImagenFoto src={fotoApartamento(apartamento.codigo, 700)} alt={apartamento.nombre} />
                  <BotonFavorito documento={documentoHuesped} codigo={apartamento.codigo} esFavorito alCambiar={recargar} />
                </div>
                <div className="tarjeta-apto__cuerpo">
                  <div>
                    <h3 className="titulo-tarjeta">{apartamento.nombre}</h3>
                    <p className="tenue pequeno">
                      Bloque {apartamento.bloque} · piso {apartamento.piso} · hasta {apartamento.capacidad} personas
                    </p>
                  </div>
                  <Boton variante="suave" pequeno onClick={() => ir('descubrir')}>
                    Buscar fechas
                  </Boton>
                </div>
              </Bisel>
            </Revelar>
          ))}
        </div>
      )}

      <DetalleReserva id={abierta} alCerrar={() => setAbierta(null)} alCambiar={recargar} />
    </div>
  );
}
