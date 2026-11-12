/**
 * Botón en forma de píldora. Si recibe `isla`, el ícono va dentro de un círculo propio, pegado al
 * borde derecho ("botón dentro de botón"); al pasar el cursor, la isla se desplaza en diagonal.
 *
 * Props:
 *  - `variante`: `primario` (espresso, por defecto), `salvia`, `suave`, `fantasma` o `peligro`.
 *  - `isla`: componente de ícono (de Phosphor) para el círculo final.
 *  - `icono`: componente de ícono que va antes del texto, sin círculo.
 *  - `pequeno`: versión compacta.
 *  - `soloIcono`: botón circular sin texto (pasar `aria-label`).
 *  - `type`: `button` por defecto, para que nunca envíe un formulario sin querer.
 */
export function Boton({ variante = 'primario', isla: Isla, icono: Icono, pequeno, soloIcono, bloque, children, className = '', type = 'button', ...resto }) {
  const clases = [
    'boton',
    variante !== 'primario' && `boton--${variante}`,
    Isla && 'boton--con-isla',
    pequeno && 'boton--pequeno',
    soloIcono && 'boton--icono',
    bloque && 'boton--bloque',
    className,
  ]
    .filter(Boolean)
    .join(' ');
  return (
    <button type={type} className={clases} {...resto}>
      {Icono && <Icono weight="light" size={soloIcono ? 20 : 18} />}
      {children}
      {Isla && (
        <span className="boton__isla">
          <Isla weight="light" />
        </span>
      )}
    </button>
  );
}
