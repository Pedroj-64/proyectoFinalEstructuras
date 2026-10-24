package co.edu.uq.estructuras.sga.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

import co.edu.uq.estructuras.sga.excepciones.EstructuraVaciaExcepcion;

/**
 * Pila genérica de comportamiento LIFO (último en entrar, primero en salir),
 * construida con nodos enlazados.
 *
 * <p>
 * El elemento disponible para consultar o retirar se encuentra en la cima.
 * Las operaciones de apilar, desapilar y consultar la cima se realizan en
 * tiempo constante.
 * </p>
 *
 * @param <T> tipo de los elementos almacenados
 */
public class PilaEnlazada<T> implements Iterable<T> {
    private Nodo<T> cima;
    private int tamano;

    /**
     * Nodo que almacena un valor y referencia al nodo inmediatamente inferior.
     *
     * @param <T> tipo del valor almacenado
     */
    private static final class Nodo<T> {
        private final T valor;
        private final Nodo<T> debajo;

        /**
         * Crea un nodo enlazado sobre otro nodo.
         *
         * @param valor  valor que almacenará el nodo
         * @param debajo nodo que quedará inmediatamente debajo
         */
        private Nodo(T valor, Nodo<T> debajo) {
            this.valor = valor;
            this.debajo = debajo;
        }
    }

    /**
     * Agrega un elemento en la cima de la pila.
     *
     * <p>
     * La operación tiene complejidad O(1).
     * </p>
     *
     * @param valor elemento que se agregará
     */
    public void apilar(T valor) {
        cima = new Nodo<>(valor, cima);
        tamano++;
    }

    /**
     * Retira y devuelve el elemento situado en la cima.
     *
     * <p>
     * La operación tiene complejidad O(1).
     * </p>
     *
     * @return elemento que estaba en la cima
     * @throws EstructuraVaciaExcepcion si la pila no contiene elementos
     */
    public T desapilar() {
        if (cima == null) {
            throw new EstructuraVaciaExcepcion("Stack");
        }
        T valor = cima.valor;
        cima = cima.debajo;
        tamano--;
        return valor;
    }

    /**
     * Consulta el elemento de la cima sin retirarlo.
     *
     * @return elemento actualmente situado en la cima
     * @throws EstructuraVaciaExcepcion si la pila no contiene elementos
     */
    public T ver() {
        if (cima == null) {
            throw new EstructuraVaciaExcepcion("Stack");
        }
        return cima.valor;
    }

    /**
     * Obtiene la cantidad actual de elementos apilados.
     *
     * @return número de elementos de la pila
     */
    public int tamano() {
        return tamano;
    }

    /**
     * Indica si la pila no contiene elementos.
     *
     * @return {@code true} si la pila está vacía; {@code false} en caso
     *         contrario
     */
    public boolean estaVacia() {
        return tamano == 0;
    }

    /**
     * Elimina todos los elementos y deja la pila vacía.
     */
    public void vaciar() {
        cima = null;
        tamano = 0;
    }

    /**
     * Crea un iterador que recorre los elementos desde la cima hasta el fondo.
     *
     * <p>
     * El primer elemento producido es el que se retiraría mediante
     * {@link #desapilar()}.
     * </p>
     *
     * @return iterador de los elementos en orden LIFO
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Nodo<T> actual = cima;

            /**
             * Indica si queda algún elemento por recorrer.
             *
             * @return {@code true} si existe un nodo pendiente
             */
            @Override
            public boolean hasNext() {
                return actual != null;
            }

            /**
             * Devuelve el valor actual y avanza hacia el fondo de la pila.
             *
             * @return valor del nodo actual
             * @throws NoSuchElementException si no quedan elementos por
             *                                recorrer
             */
            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException();
                }
                T valor = actual.valor;
                actual = actual.debajo;
                return valor;
            }
        };
    }
}
