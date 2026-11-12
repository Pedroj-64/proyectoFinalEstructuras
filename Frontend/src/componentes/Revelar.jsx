import { useEffect, useRef, useState } from 'react';

/**
 * Hace que su contenido aparezca con un fundido hacia arriba la primera vez que entra en pantalla.
 * Usa `IntersectionObserver` (no escucha `scroll`, así no provoca reflujos) y deja de observar al revelar.
 *
 * Props: `retraso` (ms) para escalonar varios elementos y `como` para cambiar la etiqueta.
 */
export function Revelar({ children, retraso = 0, como: Etiqueta = 'div', className = '', style, ...resto }) {
  const referencia = useRef(null);
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const elemento = referencia.current;
    if (!elemento) return undefined;
    const observador = new IntersectionObserver(
      ([entrada]) => {
        if (entrada.isIntersecting) {
          setVisible(true);
          observador.disconnect();
        }
      },
      { rootMargin: '0px 0px -6% 0px' },
    );
    observador.observe(elemento);
    return () => observador.disconnect();
  }, []);

  return (
    <Etiqueta
      ref={referencia}
      className={`revelar ${visible ? 'revelar--visible' : ''} ${className}`}
      style={{ ...style, '--retraso': `${retraso}ms` }}
      {...resto}
    >
      {children}
    </Etiqueta>
  );
}
