import {
  Broom,
  CalendarBlank,
  ChartLineUp,
  Compass,
  Door,
  GearSix,
  House,
  MapTrifold,
  Plugs,
  Receipt,
  Users,
  Buildings,
} from '@phosphor-icons/react';
import { lazy } from 'react';
import { ROLES } from '../contexto/Sesion.jsx';

/**
 * Registro de pantallas. Cada una se carga con `lazy`: el navegador solo descarga el código de una
 * pantalla la primera vez que se visita (división de código por rutas, sin librería de rutas).
 */
const REGISTRO = {
  descubrir: { etiqueta: 'Reservar', icono: Compass, Componente: lazy(() => import('../paginas/Descubrir.jsx')) },
  misReservas: { etiqueta: 'Mis estancias', icono: Receipt, Componente: lazy(() => import('../paginas/MisReservas.jsx')) },
  panel: { etiqueta: 'Panel', icono: House, Componente: lazy(() => import('../paginas/Panel.jsx')) },
  reservas: { etiqueta: 'Reservas', icono: Door, Componente: lazy(() => import('../paginas/Reservas.jsx')) },
  apartamentos: { etiqueta: 'Apartamentos', icono: Buildings, Componente: lazy(() => import('../paginas/Apartamentos.jsx')) },
  calendario: { etiqueta: 'Calendario', icono: CalendarBlank, Componente: lazy(() => import('../paginas/Calendario.jsx')) },
  preparacion: { etiqueta: 'Preparación', icono: Broom, Componente: lazy(() => import('../paginas/Preparacion.jsx')) },
  canal: { etiqueta: 'Canal externo', icono: Plugs, Componente: lazy(() => import('../paginas/CanalExterno.jsx')) },
  huespedes: { etiqueta: 'Huéspedes', icono: Users, Componente: lazy(() => import('../paginas/Huespedes.jsx')) },
  mapa: { etiqueta: 'Mapa', icono: MapTrifold, Componente: lazy(() => import('../paginas/MapaEdificio.jsx')) },
  reportes: { etiqueta: 'Reportes', icono: ChartLineUp, Componente: lazy(() => import('../paginas/Reportes.jsx')) },
  administracion: { etiqueta: 'Administración', icono: GearSix, Componente: lazy(() => import('../paginas/Administracion.jsx')) },
};

const OPERACION = ['panel', 'reservas', 'descubrir', 'apartamentos', 'calendario', 'preparacion', 'canal', 'huespedes', 'mapa'];

/** Pantallas visibles para cada rol, en el orden en que aparecen en el menú. */
const MENU_POR_ROL = {
  [ROLES.HUESPED]: ['descubrir', 'misReservas'],
  [ROLES.RECEPCIONISTA]: OPERACION,
  [ROLES.ADMINISTRADOR]: [...OPERACION, 'reportes', 'administracion'],
};

/** Lista de pantallas (con su `id`) que puede ver un rol. */
export function paginasDelRol(rol) {
  return MENU_POR_ROL[rol].map((id) => ({ id, ...REGISTRO[id] }));
}
