/**
 * Contenedor de "doble bisel": una carcasa translúcida con un núcleo interior, como una placa de
 * vidrio sobre una bandeja de aluminio. Los radios son concéntricos (ver `.bisel` en sistema.css).
 *
 * Props:
 *  - `compacto`: carcasa más pequeña, para tarjetas densas.
 *  - `sinRelleno`: el núcleo no añade espacio interior (útil para fotos a sangre).
 *  - `oscuro`: variante de espresso para destacar una pieza.
 *  - `interactivo`: se eleva suavemente al pasar el cursor.
 *  - `como`: etiqueta HTML de la carcasa (`section` por defecto).
 */
export function Bisel({ children, compacto, sinRelleno, oscuro, interactivo, como: Etiqueta = 'section', className = '', ...resto }) {
  const clases = [
    'bisel',
    compacto && 'bisel--compacto',
    sinRelleno && 'bisel--sin-relleno',
    oscuro && 'bisel--oscuro',
    interactivo && 'bisel--interactivo',
    className,
  ]
    .filter(Boolean)
    .join(' ');
  return (
    <Etiqueta className={clases} {...resto}>
      <div className="bisel__nucleo">{children}</div>
    </Etiqueta>
  );
}
