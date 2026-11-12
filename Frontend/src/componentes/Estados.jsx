import { Tray } from '@phosphor-icons/react';

/** Marcador de carga: bloques que "respiran" en lugar de un spinner. `bloques` = cuántos mostrar. */
export function Cargando({ bloques = 3 }) {
  return (
    <div className="pila" role="status" aria-label="Cargando">
      {Array.from({ length: bloques }, (_, indice) => (
        <div key={indice} className="esqueleto" style={{ animationDelay: `${indice * 150}ms` }} />
      ))}
    </div>
  );
}

/** Estado vacío con ícono, título y texto de apoyo; `children` admite una acción. */
export function Vacio({ icono: Icono = Tray, titulo, texto, children }) {
  return (
    <div className="vacio">
      <span className="vacio__icono">
        <Icono weight="light" />
      </span>
      <strong>{titulo}</strong>
      {texto && <p>{texto}</p>}
      {children}
    </div>
  );
}
