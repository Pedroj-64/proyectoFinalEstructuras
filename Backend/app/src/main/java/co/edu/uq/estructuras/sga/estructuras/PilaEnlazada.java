package co.edu.uq.estructuras.sga.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

import co.edu.uq.estructuras.sga.excepciones.EstructuraVaciaExcepcion;

public class PilaEnlazada<T> implements Iterable<T> {

    /// Nodo con su valor y el enlace al nodo de abajo.
    private static final class Nodo<T> {
        /// Valor guardado.
        private final T valor;
        /// Nodo de abajo.
        private final Nodo<T> debajo;

        /// Crea un nodo encima de `debajo`.
        private Nodo(T valor, Nodo<T> debajo) {
            this.valor = valor;
            this.debajo = debajo;
        }
    }

    /// Cima de la pila.
    private Nodo<T> cima;
    /// Cantidad de elementos.
    private int tamano;

    /// Apila el valor en la cima en O(1).
    public void apilar(T valor) {
        cima = new Nodo<>(valor, cima);
        tamano++;
    }

    /// Saca y devuelve la cima en O(1). Lanza [EstructuraVaciaExcepcion] si está vacía.
    public T desapilar() {
        if (cima == null) {
            throw new EstructuraVaciaExcepcion("Stack");
        }
        T valor = cima.valor;
        cima = cima.debajo;
        tamano--;
        return valor;
    }

    /// Cima de la pila sin sacarla. Lanza [EstructuraVaciaExcepcion] si está vacía.
    public T ver() {
        if (cima == null) {
            throw new EstructuraVaciaExcepcion("Stack");
        }
        return cima.valor;
    }

    /// Cantidad de elementos apilados.
    public int tamano() {
        return tamano;
    }

    /// Indica si la pila no tiene elementos.
    public boolean estaVacia() {
        return tamano == 0;
    }

    /// Vacía la pila.
    public void vaciar() {
        cima = null;
        tamano = 0;
    }

    /// Recorre desde la cima hasta el fondo.
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            /// Nodo que se devolverá a continuación.
            private Nodo<T> actual = cima;

            /// Quedan elementos mientras el nodo actual exista.
            @Override
            public boolean hasNext() {
                return actual != null;
            }

            /// Devuelve el valor actual y baja al siguiente nodo.
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
