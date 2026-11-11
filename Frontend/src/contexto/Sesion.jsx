import { createContext, useCallback, useContext, useMemo, useState } from 'react';

/** Roles de la aplicación: el rol activo decide qué pantallas se ven. */
export const ROLES = { HUESPED: 'HUESPED', RECEPCIONISTA: 'RECEPCIONISTA', ADMINISTRADOR: 'ADMINISTRADOR' };

/** Nombre de cada rol para mostrarlo en pantalla. */
export const ETIQUETAS_ROL = {
  HUESPED: 'Huésped',
  RECEPCIONISTA: 'Recepcionista',
  ADMINISTRADOR: 'Administrador',
};

const CLAVE_ALMACENAMIENTO = 'sga.sesion';
const SESION_INICIAL = { rol: ROLES.RECEPCIONISTA, documentoHuesped: '1094000001' };

/** Lee la sesión guardada en el navegador, o la sesión inicial si no hay una (o no se puede leer). */
function restaurar() {
  try {
    const guardada = localStorage.getItem(CLAVE_ALMACENAMIENTO);
    if (guardada) return JSON.parse(guardada);
  } catch {
    // almacenamiento no disponible: se usa la sesión inicial
  }
  return SESION_INICIAL;
}

const ContextoSesion = createContext(null);

/**
 * Guarda el rol activo y el huésped que se "simula" cuando el rol es huésped.
 * No hay inicio de sesión real: el selector de rol de la barra superior hace de usuario de demostración.
 */
export function ProveedorSesion({ children }) {
  const [sesion, setSesion] = useState(restaurar);

  const actualizar = useCallback((siguiente) => {
    setSesion(siguiente);
    try {
      localStorage.setItem(CLAVE_ALMACENAMIENTO, JSON.stringify(siguiente));
    } catch {
      // sin almacenamiento: la sesión solo vive mientras la pestaña esté abierta
    }
  }, []);

  const valor = useMemo(
    () => ({
      rol: sesion.rol,
      documentoHuesped: sesion.documentoHuesped,
      etiquetaRol: ETIQUETAS_ROL[sesion.rol],
      cambiarRol: (rol) => actualizar({ ...sesion, rol }),
      cambiarHuesped: (documentoHuesped) => actualizar({ ...sesion, documentoHuesped }),
      tieneRol: (...roles) => roles.includes(sesion.rol),
    }),
    [sesion, actualizar],
  );

  return <ContextoSesion.Provider value={valor}>{children}</ContextoSesion.Provider>;
}

/** Acceso a la sesión actual desde cualquier componente. */
export function useSesion() {
  return useContext(ContextoSesion);
}
