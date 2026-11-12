import { ArrowLeft } from '@phosphor-icons/react';
import { useNavegacion } from '../navegacion/Navegacion.jsx';
import { Boton } from './Boton.jsx';
import { Revelar } from './Revelar.jsx';

/**
 * Cabecera de cada pantalla: etiqueta superior, título editorial (admite `<em>` para el acento en
 * cursiva), descripción y, a la derecha, las acciones que reciba como `children`.
 * Muestra el botón "Atrás" cuando hay historial en la pila de navegación.
 */
export function Encabezado({ eyebrow, titulo, descripcion, children }) {
  const { puedeVolver, atras } = useNavegacion();
  return (
    <Revelar como="header" className="encabezado">
      <div className="encabezado__texto">
        <div className="fila">
          {puedeVolver && (
            <Boton variante="suave" pequeno icono={ArrowLeft} onClick={atras}>
              Atrás
            </Boton>
          )}
          <span className="eyebrow">{eyebrow}</span>
        </div>
        <h1 className="titulo-pagina">{titulo}</h1>
        {descripcion && <p className="subtitulo">{descripcion}</p>}
      </div>
      {children && <div className="encabezado__acciones">{children}</div>}
    </Revelar>
  );
}
