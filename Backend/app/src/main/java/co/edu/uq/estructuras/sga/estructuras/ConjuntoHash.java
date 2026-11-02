package co.edu.uq.estructuras.sga.estructuras;

import java.util.Iterator;
import java.util.function.IntFunction;

/**
 * Conjunto de valores únicos respaldado por {@link MapaHash}.
 *
 * @param <T> tipo de los elementos
 */
public class ConjuntoHash<T> implements Iterable<T> {
    private final MapaHash<T, Boolean> elementos = new MapaHash<>();

    /**
     * Agrega un valor si no estaba presente.
     *
     * @param valor elemento que se agregará
     * @return {@code true} si se agregó; {@code false} si ya existía
     */
    public boolean agregar(T valor) {
        return elementos.poner(valor, Boolean.TRUE) == null;
    }

    /**
     * Elimina un valor del conjunto.
     *
     * @param valor elemento que se eliminará
     * @return {@code true} si estaba presente
     */
    public boolean quitar(T valor) {
        return elementos.quitar(valor) != null;
    }

    /**
     * Comprueba si un valor pertenece al conjunto.
     *
     * @param valor elemento que se consultará
     * @return {@code true} si está presente
     */
    public boolean contiene(T valor) {
        return elementos.contieneLlave(valor);
    }

    /**
     * Obtiene la cantidad de elementos.
     *
     * @return número de elementos almacenados
     */
    public int tamano() {
        return elementos.tamano();
    }

    /**
     * Indica si el conjunto está vacío.
     *
     * @return {@code true} si no contiene elementos
     */
    public boolean estaVacia() {
        return elementos.estaVacia();
    }

    /** Elimina todos los elementos del conjunto. */
    public void vaciar() {
        elementos.vaciar();
    }

    /**
     * Copia los elementos a un arreglo creado por el generador indicado.
     *
     * @param generador función que crea el arreglo del tamaño necesario
     * @return arreglo con los elementos del conjunto
     */
    public T[] aArreglo(IntFunction<T[]> generador) {
        return elementos.llaves().aArreglo(generador);
    }

    /**
     * Recorre los elementos sin un orden garantizado.
     *
     * @return iterador de los elementos
     */
    @Override
    public Iterator<T> iterator() {
        return elementos.llaves().iterator();
    }
}
