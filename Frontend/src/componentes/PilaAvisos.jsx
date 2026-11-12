import { CheckCircle, Info, Warning } from '@phosphor-icons/react';
import { useAvisos } from '../contexto/Avisos.jsx';

const ICONOS = { exito: CheckCircle, error: Warning, info: Info };

/** Dibuja los avisos emergentes del contexto `Avisos`; un clic descarta el aviso. */
export function PilaAvisos() {
  const { avisos, descartar } = useAvisos();
  return (
    <div className="avisos" aria-live="polite">
      {avisos.map((aviso) => {
        const Icono = ICONOS[aviso.tipo];
        return (
          <button key={aviso.id} type="button" className={`aviso aviso--${aviso.tipo}`} onClick={() => descartar(aviso.id)}>
            <span className="aviso__icono">
              <Icono weight="light" />
            </span>
            <div>
              <strong>{aviso.titulo}</strong>
              <span>{aviso.mensaje}</span>
            </div>
          </button>
        );
      })}
    </div>
  );
}
