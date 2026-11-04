package co.edu.uq.estructuras.sga.core.edificio;

/// Punto del mapa del edificio. En los apartamentos, `id` es el código del
/// apartamento.
/// `x` y `y` son coordenadas de dibujo que usa el mapa interactivo.
public record NodoEdificio(String id, TipoNodo tipo, String etiqueta, String bloque, int piso, double x, double y) {
}
