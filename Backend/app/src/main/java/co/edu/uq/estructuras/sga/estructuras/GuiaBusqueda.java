package co.edu.uq.estructuras.sga.estructuras;

public interface GuiaBusqueda<T> {

    /// Indica si el valor visitado es una coincidencia.
    boolean coincide(T valor);

    /// Indica si vale la pena bajar al subárbol izquierdo de este nodo. Por defecto, sí.
    default boolean explorarIzquierda(T valor) {
        return true;
    }

    /// Indica si vale la pena bajar al subárbol derecho de este nodo. Por defecto, sí.
    default boolean explorarDerecha(T valor) {
        return true;
    }
}