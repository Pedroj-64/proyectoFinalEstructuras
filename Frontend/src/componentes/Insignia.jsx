/** Etiqueta de estado con punto de color. `tono`: `salvia`, `azul`, `ambar`, `rojo` o `neutro`. */
export function Insignia({ tono = 'neutro', children }) {
  return <span className={`insignia ${tono !== 'neutro' ? `insignia--${tono}` : ''}`}>{children}</span>;
}
