import { X } from '@phosphor-icons/react';
import { useEffect, useRef } from 'react';
import { Bisel } from './Bisel.jsx';
import { Boton } from './Boton.jsx';

/**
 * Diálogo modal construido sobre el elemento nativo `<dialog>` (foco atrapado, Esc para cerrar y
 * capa superior gratis). El contenido solo se monta mientras está abierto, así los formularios y las
 * consultas internas empiezan limpios cada vez.
 *
 * Props: `abierto`, `alCerrar`, `eyebrow`, `titulo`, `ancho` (px) y `children`.
 */
export function Modal({ abierto, alCerrar, eyebrow, titulo, ancho = 640, children }) {
  const referencia = useRef(null);

  useEffect(() => {
    const dialogo = referencia.current;
    if (abierto && !dialogo.open) dialogo.showModal();
    if (!abierto && dialogo.open) dialogo.close();
  }, [abierto]);

  return (
    <dialog
      ref={referencia}
      className="modal"
      style={{ '--ancho-modal': `${ancho}px` }}
      onClose={alCerrar}
      // Un clic sobre el fondo oscurece el propio <dialog>: ahí se cierra.
      onClick={(evento) => evento.target === referencia.current && alCerrar()}
    >
      {abierto && (
        <Bisel>
          <div className="modal__cuerpo">
            <div className="modal__cabecera">
              <div>
                {eyebrow && <span className="eyebrow">{eyebrow}</span>}
                <h2 className="titulo-seccion">{titulo}</h2>
              </div>
              <Boton variante="suave" soloIcono onClick={alCerrar} aria-label="Cerrar">
                <X weight="light" size={18} />
              </Boton>
            </div>
            {children}
          </div>
        </Bisel>
      )}
    </dialog>
  );
}
