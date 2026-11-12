/**
 * Fotografías de interiores (Unsplash) para ambientar la interfaz. El backend no guarda fotos de los
 * apartamentos, así que cada uno recibe una de forma estable según su código.
 * Si una foto no carga, `ImagenFoto` muestra un degradado de respaldo.
 */
const BASE = 'https://images.unsplash.com/';

const IDENTIFICADORES = [
  'photo-1522708323590-d24dbb6b0267',
  'photo-1493809842364-78817add7ffb',
  'photo-1560448204-e02f11c3d0e2',
  'photo-1505693416388-ac5ce068fe85',
  'photo-1586023492125-27b2c045efd7',
  'photo-1502672260266-1c1ef2d93688',
];

/** URL de una foto con el ancho pedido (Unsplash la recorta y comprime en su servidor). */
function url(identificador, ancho) {
  return `${BASE}${identificador}?auto=format&fit=crop&w=${ancho}&q=70`;
}

/** Foto estable para un apartamento a partir de su código (`A-101`, `B-202`…). */
export function fotoApartamento(codigo, ancho = 900) {
  let suma = 0;
  for (const letra of codigo ?? '') suma += letra.charCodeAt(0);
  return url(IDENTIFICADORES[suma % IDENTIFICADORES.length], ancho);
}

/** Foto grande para la portada. */
export const FOTO_PORTADA = url('photo-1502672260266-1c1ef2d93688', 1400);

/** Foto secundaria de la portada. */
export const FOTO_PORTADA_2 = url('photo-1586023492125-27b2c045efd7', 700);
