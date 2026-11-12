import { ArrowUpRight, Bed, MagnifyingGlass, Users } from '@phosphor-icons/react';
import { useState } from 'react';
import { BotonFavorito } from '../componentes/BotonFavorito.jsx';
import { Bisel } from '../componentes/Bisel.jsx';
import { Boton } from '../componentes/Boton.jsx';
import { Cargando, Vacio } from '../componentes/Estados.jsx';
import { ImagenFoto } from '../componentes/ImagenFoto.jsx';
import { Insignia } from '../componentes/Insignia.jsx';
import { ModalReserva } from '../componentes/ModalReserva.jsx';
import { Revelar } from '../componentes/Revelar.jsx';
import { useReloj } from '../contexto/Reloj.jsx';
import { ROLES, useSesion } from '../contexto/Sesion.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { useNavegacion } from '../navegacion/Navegacion.jsx';
import { apartamentosApi, huespedesApi } from '../servicios/api.js';
import { formatearDinero } from '../utilidades/dinero.js';
import { sumarDias } from '../utilidades/fechas.js';
import { FOTO_PORTADA, FOTO_PORTADA_2, fotoApartamento } from '../utilidades/fotos.js';

/** Anchos del bento para que las tarjetas se alternen: grande-pequeña, pequeña-grande. */
const ANCHOS = ['c7', 'c5', 'c5', 'c7'];

/**
 * Portada + buscador de disponibilidad + resultados. Es la pantalla principal del huésped y también
 * la herramienta de "nueva reserva" del personal (que elige el titular dentro del modal).
 */
export default function Descubrir() {
  const { hoy } = useReloj();
  const { rol, documentoHuesped } = useSesion();
  const { ir } = useNavegacion();
  const esHuesped = rol === ROLES.HUESPED;

  // El formulario guarda solo lo que el usuario cambió; el resto sale de los valores por defecto,
  // que dependen de "hoy" según el servidor (puede ser una fecha simulada).
  const porDefecto = { ingreso: sumarDias(hoy, 1), salida: sumarDias(hoy, 4), huespedes: 2, ordenar: 'PRECIO' };
  const [edicion, setEdicion] = useState({});
  const formulario = { ...porDefecto, ...edicion };
  const [aplicados, setAplicados] = useState(null);
  const criterios = aplicados ?? porDefecto;
  const cambiar = (campo) => (evento) => setEdicion((previo) => ({ ...previo, [campo]: evento.target.value }));

  const { datos, cargando, actualizando } = useConsulta(
    () => apartamentosApi.disponibilidad(criterios.ingreso, criterios.salida, criterios.huespedes, criterios.ordenar),
    [criterios.ingreso, criterios.salida, criterios.huespedes, criterios.ordenar],
  );
  // `useConsulta` conserva la lista anterior (atenuada) mientras llega la nueva, así no parpadea.
  const resultados = datos;

  const { datos: favoritos, recargar: recargarFavoritos } = useConsulta(() => (esHuesped ? huespedesApi.favoritos(documentoHuesped) : Promise.resolve([])), [esHuesped, documentoHuesped]);
  const codigosFavoritos = new Set((favoritos ?? []).map((apartamento) => apartamento.codigo));

  const [seleccion, setSeleccion] = useState(null);
  const fechasValidas = formulario.salida > formulario.ingreso;

  return (
    <div className="pagina">
      <section className="portada">
        <Revelar className="portada__texto">
          <span className="eyebrow">Aparta-hotel · Armenia, Quindío</span>
          <h1 className="titulo-pagina">
            Tu estancia, <em>sin ruido</em>, entre montañas de café.
          </h1>
          <p className="subtitulo">Suites completas con cocina y balcón, a minutos de los cafetales. Elige tus fechas y mira al instante qué apartamentos están libres y cuánto cuestan.</p>
        </Revelar>

        <Revelar retraso={150} className="portada__fotos">
          <Bisel sinRelleno className="portada__principal">
            <ImagenFoto src={FOTO_PORTADA} alt="Sala luminosa con plantas" />
          </Bisel>
          <Bisel compacto sinRelleno className="portada__secundaria">
            <ImagenFoto src={FOTO_PORTADA_2} alt="Rincón con sillón amarillo" />
          </Bisel>
          <div className="portada__chip">
            <strong>{resultados ? resultados.length : '—'}</strong>
            <span>apartamentos libres para tus fechas</span>
          </div>
        </Revelar>
      </section>

      <Revelar como="section">
        <Bisel>
          <form
            className="formulario"
            onSubmit={(evento) => {
              evento.preventDefault();
              setAplicados(formulario);
            }}
          >
            <label className="campo">
              <span>Ingreso</span>
              <input type="date" value={formulario.ingreso} min={hoy} onChange={cambiar('ingreso')} />
            </label>
            <label className="campo">
              <span>Salida</span>
              <input type="date" value={formulario.salida} min={sumarDias(formulario.ingreso, 1)} onChange={cambiar('salida')} />
            </label>
            <label className="campo">
              <span>Huéspedes</span>
              <input type="number" min="1" max="12" value={formulario.huespedes} onChange={cambiar('huespedes')} />
            </label>
            <label className="campo">
              <span>Ordenar por</span>
              <select value={formulario.ordenar} onChange={cambiar('ordenar')}>
                <option value="PRECIO">Precio</option>
                <option value="CAPACIDAD">Capacidad</option>
                <option value="HABITACIONES">Habitaciones</option>
              </select>
            </label>
            <Boton type="submit" isla={MagnifyingGlass} disabled={!fechasValidas}>
              Ver disponibles
            </Boton>
          </form>
        </Bisel>
      </Revelar>

      <section className="seccion" aria-live="polite">
        <div className="seccion__cabecera">
          <h2 className="titulo-seccion">
            Disponibles <em>para ti</em>
          </h2>
          {resultados && <span className="tenue">{resultados.length} resultado(s)</span>}
        </div>

        {cargando && <Cargando bloques={3} />}
        {resultados?.length === 0 && <Vacio icono={Bed} titulo="Nada libre en esas fechas" texto="Prueba con otras fechas o con menos huéspedes." />}

        <div className="bento" style={{ opacity: actualizando ? 0.5 : 1, transition: 'opacity 0.6s var(--muelle)' }}>
          {(resultados ?? []).map(({ apartamento, tarifaPorPersona, totalEstimado }, indice) => (
            <Revelar key={apartamento.codigo} retraso={(indice % 2) * 120} className={ANCHOS[indice % ANCHOS.length]}>
              <Bisel sinRelleno interactivo className="tarjeta-apto">
                <div className="tarjeta-apto__foto">
                  <ImagenFoto src={fotoApartamento(apartamento.codigo)} alt={apartamento.nombre} />
                  <span className="tarjeta-apto__codigo">{apartamento.codigo}</span>
                  {esHuesped && <BotonFavorito documento={documentoHuesped} codigo={apartamento.codigo} esFavorito={codigosFavoritos.has(apartamento.codigo)} alCambiar={recargarFavoritos} />}
                </div>
                <div className="tarjeta-apto__cuerpo">
                  <div className="fila separado">
                    <h3 className="titulo-tarjeta">{apartamento.nombre}</h3>
                    <Insignia tono="salvia">Bloque {apartamento.bloque}</Insignia>
                  </div>
                  <div className="fila tenue pequeno">
                    <span className="fila" style={{ gap: '0.35rem' }}>
                      <Bed weight="light" size={16} /> {apartamento.habitaciones} hab.
                    </span>
                    <span className="fila" style={{ gap: '0.35rem' }}>
                      <Users weight="light" size={16} /> hasta {apartamento.capacidad}
                    </span>
                    <span>Piso {apartamento.piso}</span>
                  </div>
                  <div className="fichas">
                    {apartamento.caracteristicas.map((caracteristica) => (
                      <span key={caracteristica} className="ficha">
                        {caracteristica}
                      </span>
                    ))}
                  </div>
                  <div className="tarjeta-apto__precio">
                    <div>
                      <span className="cifra">{formatearDinero(totalEstimado)}</span>
                      <p className="pequeno tenue">{formatearDinero(tarifaPorPersona)} por persona y noche</p>
                    </div>
                    <Boton isla={ArrowUpRight} onClick={() => setSeleccion({ apartamento, ingreso: criterios.ingreso, salida: criterios.salida, huespedes: Number(criterios.huespedes) })}>
                      Reservar
                    </Boton>
                  </div>
                </div>
              </Bisel>
            </Revelar>
          ))}
        </div>
      </section>

      <ModalReserva
        reserva={seleccion}
        alCerrar={() => setSeleccion(null)}
        alCreada={(creada) => {
          setSeleccion(null);
          if (esHuesped) ir('misReservas');
          else ir('reservas', { abrir: creada.id });
        }}
      />
    </div>
  );
}
