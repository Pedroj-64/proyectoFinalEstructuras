package co.edu.uq.estructuras.sga.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

import co.edu.uq.estructuras.sga.excepciones.EstructuraVaciaExcepcion;

/**
 * Cola FIFO genérica construida con nodos simplemente enlazados.
 *
 * <p>Los elementos se agregan por el fondo y se retiran por el frente, por lo
 * que ambas operaciones se realizan en tiempo constante. Se usa para
 * procesar, en orden de llegada, las solicitudes del canal externo de
 * reservas.</p>
 *
 * @param <T> tipo de elemento almacenado
 */
public class ColaEnlazada<T> implements Iterable<T> {
    private Nodo<T> frente;
    private Nodo<T> fondo;
    private int tamano;

    /**
     * Nodo simple que almacena un valor y la referencia al nodo siguiente.
     *
     * @param <T> tipo del valor almacenado
     */
    private static final class Nodo<T> {
        private final T valor;
        private Nodo<T> siguiente;
        /**
         * Crea un nodo sin sucesor.
         *
         * @param valor valor que almacenará el nodo
         */
        private Nodo(T valor) {
            this.valor = valor;
        }
    }

    /**
     * Agrega un elemento al fondo de la cola.
     *
     * @param valor elemento que se agregará
     */
    public void encolar(T valor) {
        Nodo<T> nodo = new Nodo<>(valor);
        if (fondo == null) {
            frente = fondo = nodo;
        } else {
            fondo.siguiente = nodo;
            fondo = nodo;
        }
        tamano++;
    }

    /**
     * Retira y devuelve el elemento que está en el frente de la cola.
     *
     * @return primer elemento que fue agregado y aún no ha sido retirado
     * @throws EstructuraVaciaExcepcion si la cola no contiene elementos
     */
    public T desencolar() {
        if (frente == null) {
            throw new EstructuraVaciaExcepcion("Queue");
        }
        T valor = frente.valor;
        frente = frente.siguiente;
        if (frente == null) {
            fondo = null;
        }
        tamano--;
        return valor;
    }

    /**
     * Consulta el elemento del frente sin retirarlo.
     *
     * @return elemento que se retiraría en la próxima operación
     * @throws EstructuraVaciaExcepcion si la cola no contiene elementos
     */
    public T ver() {
        if (frente == null) {
            throw new EstructuraVaciaExcepcion("Queue");
        }
        return frente.valor;
    }

    /**
     * Obtiene la cantidad de elementos en espera.
     *
     * @return número actual de elementos
     */
    public int tamano() {
        return tamano;
    }

    /**
     * Indica si la cola no contiene elementos.
     *
     * @return {@code true} si la cola está vacía; {@code false} en caso
     *         contrario
     */
    public boolean estaVacia() {
        return tamano == 0;
    }

    /**
     * Elimina todos los elementos de la cola y la deja vacía.
     */
    public void vaciar() {
        frente = fondo = null;
        tamano = 0;
    }

    /**
     * Crea un iterador que recorre los elementos desde el frente hasta el
     * fondo, respetando el orden de llegada.
     *
     * @return iterador de los elementos de la cola
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            /** Nodo cuyo valor se devolverá en la próxima llamada a {@code next}. */
            private Nodo<T> actual = frente;

            /**
             * Indica si todavía queda un elemento por recorrer.
             *
             * @return {@code true} si existe un nodo pendiente
             */
            @Override
            public boolean hasNext() {
                return actual != null;
            }

            /**
             * Devuelve el elemento actual y avanza hacia el fondo de la cola.
             *
             * @return valor del nodo actual
             * @throws NoSuchElementException si no quedan elementos por
             *         recorrer
             */
            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException();
                }
                T valor = actual.valor;
                actual = actual.siguiente;
                return valor;
            }
        };
    }
}
