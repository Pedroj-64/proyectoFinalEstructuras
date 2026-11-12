import { GearSix, Key, Mountains, UserCircle } from '@phosphor-icons/react';
import { useEffect, useState } from 'react';
import { ETIQUETAS_ROL, ROLES, useSesion } from '../contexto/Sesion.jsx';
import { useReloj } from '../contexto/Reloj.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { useNavegacion } from '../navegacion/Navegacion.jsx';
import { huespedesApi } from '../servicios/api.js';
import { formatearFechaHora } from '../utilidades/fechas.js';

const DESCRIPCION_ROL = {
  [ROLES.HUESPED]: { icono: UserCircle, texto: 'Explora y reserva tu estancia' },
  [ROLES.RECEPCIONISTA]: { icono: Key, texto: 'Operación diaria del edificio' },
  [ROLES.ADMINISTRADOR]: { icono: GearSix, texto: 'Reportes, tarifas y personal' },
};

/** Lista desplegable para elegir qué huésped se simula cuando el rol es "Huésped". */
function SelectorHuesped() {
  const { documentoHuesped, cambiarHuesped } = useSesion();
  const { datos } = useConsulta(huespedesApi.listar);
  return (
    <label className="campo">
      <span>Huésped simulado</span>
      <select value={documentoHuesped} onChange={(evento) => cambiarHuesped(evento.target.value)}>
        {(datos ?? []).map((huesped) => (
          <option key={huesped.documento} value={huesped.documento}>
            {huesped.nombreCompleto}
          </option>
        ))}
      </select>
    </label>
  );
}

/**
 * Barra de navegación: una píldora de vidrio que flota separada del borde superior.
 * En escritorio muestra íconos (el activo se expande con su nombre); en móvil solo marca y menú.
 * El botón hamburguesa abre un menú a pantalla completa con todas las pantallas, el cambio de rol y el reloj.
 */
export function Isla() {
  const [abierto, setAbierto] = useState(false);
  const { rol, cambiarRol } = useSesion();
  const { estado, hoy } = useReloj();
  const { pagina, permitidas, irRaiz } = useNavegacion();

  // Con el menú abierto: Esc lo cierra y la página de fondo no se desplaza.
  useEffect(() => {
    if (!abierto) return undefined;
    const alTeclear = (evento) => evento.key === 'Escape' && setAbierto(false);
    document.addEventListener('keydown', alTeclear);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', alTeclear);
      document.body.style.overflow = '';
    };
  }, [abierto]);

  const navegar = (id) => {
    irRaiz(id);
    setAbierto(false);
  };

  return (
    <>
      <nav className="isla" aria-label="Principal">
        <button type="button" className="isla__marca" onClick={() => navegar(permitidas[0].id)} aria-label="Ir al inicio">
          <span className="isla__logo">
            <Mountains weight="light" />
          </span>
          <span className="isla__nombre">Casa Quindío</span>
        </button>

        <div className="isla__enlaces">
          {permitidas.map(({ id, etiqueta, icono: Icono }) => (
            <button
              key={id}
              type="button"
              className="isla__enlace"
              aria-current={pagina.id === id ? 'page' : undefined}
              aria-label={etiqueta}
              title={etiqueta}
              onClick={() => navegar(id)}
            >
              <Icono weight="light" />
              <span>
                <b>{etiqueta}</b>
              </span>
            </button>
          ))}
        </div>

        <span className="isla__actual">{pagina.etiqueta}</span>

        <button type="button" className="hamburguesa" aria-expanded={abierto} aria-controls="menu-principal" aria-label={abierto ? 'Cerrar menú' : 'Abrir menú'} onClick={() => setAbierto(!abierto)}>
          <i />
          <i />
        </button>
      </nav>

      <div id="menu-principal" className="menu" data-abierto={abierto} inert={!abierto}>
        <div className="menu__interior">
          <div className="menu__enlaces">
            {permitidas.map(({ id, etiqueta }, indice) => (
              <div key={id} className="menu__reveal">
                <button type="button" className="menu__enlace" style={{ '--i': indice }} aria-current={pagina.id === id ? 'page' : undefined} onClick={() => navegar(id)}>
                  <small>{String(indice + 1).padStart(2, '0')}</small>
                  {etiqueta}
                </button>
              </div>
            ))}
          </div>

          <div className="menu__lateral">
            <div className="menu__reveal">
              <div style={{ '--i': permitidas.length }}>
                <h3>Quién eres hoy</h3>
                <div className="opciones-rol" role="radiogroup" aria-label="Rol">
                  {Object.values(ROLES).map((valor) => {
                    const { icono: Icono, texto } = DESCRIPCION_ROL[valor];
                    return (
                      <button key={valor} type="button" role="radio" aria-checked={rol === valor} className="opcion-rol" onClick={() => cambiarRol(valor)}>
                        <Icono weight="light" />
                        <span>
                          {ETIQUETAS_ROL[valor]}
                          <small>{texto}</small>
                        </span>
                      </button>
                    );
                  })}
                </div>
              </div>
            </div>

            {rol === ROLES.HUESPED && (
              <div className="menu__reveal">
                <div style={{ '--i': permitidas.length + 1 }}>
                  <SelectorHuesped />
                </div>
              </div>
            )}

            <div className="menu__reveal">
              <div style={{ '--i': permitidas.length + 2 }}>
                <h3>Reloj del sistema</h3>
                <p>{estado ? formatearFechaHora(estado.ahora) : hoy}</p>
                <p className="pequeno tenue">{estado?.simulado ? 'Hora simulada por el administrador' : 'Hora real del servidor'}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
