import { useState } from 'react';

/**
 * Foto con carga diferida. Si no se puede descargar (sin internet, enlace caído) se muestra un
 * degradado cálido en su lugar, así el diseño nunca queda con un hueco roto.
 */
export function ImagenFoto({ src, alt = '', className = '' }) {
  const [fallo, setFallo] = useState(false);
  if (fallo) return <div className={`foto foto--respaldo ${className}`} role="img" aria-label={alt} />;
  return <img className={`foto ${className}`} src={src} alt={alt} loading="lazy" decoding="async" onError={() => setFallo(true)} />;
}
