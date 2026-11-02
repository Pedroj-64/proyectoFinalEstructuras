package co.edu.uq.estructuras.sga.estructuras;

/**
 * Estrategia de búsqueda para recorrer selectivamente un árbol de búsqueda.
 *
 * @param <T> tipo de los valores del árbol
 */
public interface GuiaBusqueda<T> {

    /**
     * Indica si el valor actual satisface la búsqueda.
     *
     * @param valor valor del nodo visitado
     * @return {@code true} si el valor coincide
     */
    boolean coincide(T valor);

    /**
     * Decide si se explora el subárbol izquierdo del nodo.
     *
     * @param valor valor del nodo actual
     * @return {@code true} para explorar el subárbol izquierdo
     */
    default boolean explorarIzquierda(T valor) {
        return true;
    }

    /**
     * Decide si se explora el subárbol derecho del nodo.
     *
     * @param valor valor del nodo actual
     * @return {@code true} para explorar el subárbol derecho
     */
    default boolean explorarDerecha(T valor) {
        return true;
    }
}