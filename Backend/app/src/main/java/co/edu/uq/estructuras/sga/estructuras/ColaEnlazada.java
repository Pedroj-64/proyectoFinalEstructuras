package co.edu.uq.estructuras.sga.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

import co.edu.uq.estructuras.sga.excepciones.EstructuraVaciaExcepcion;

/// Cola FIFO genérica construida con nodos simplemente enlazados.
///
/// Se usa para: las solicitudes del canal externo de reservas, procesadas en orden de llegada.
///
/// @param <T> tipo de elemento
public class ColaEnlazada<T> implements Iterable<T> {

    /// Nodo con su valor y el enlace al siguiente en la fila.
    private static final class Nodo<T> {
        /// Valor guardado.
        private final T valor;
        /// Siguiente nodo hacia el final de la fila.
        private Nodo<T> siguiente;

        /// Crea un nodo suelto.
        private Nodo(T valor) {
            this.valor = valor;
        }
    }

    /// Frente de la fila (el próximo en salir).
    private Nodo<T> frente;
    /// Final de la fila (el último en llegar).
    private Nodo<T> fondo;
    /// Cantidad de elementos.
    private int tamano;

    /// Agrega el valor al final de la cola en O(1).
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

    /// Quita y devuelve el primero de la cola en O(1). Lanza [EstructuraVaciaExcepcion] si está vacía.
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

    /// Primero de la cola sin quitarlo. Lanza [EstructuraVaciaExcepcion] si está vacía.
    public T ver() {
        if (frente == null) {
            throw new EstructuraVaciaExcepcion("Queue");
        }
        return frente.valor;
    }

    /// Cantidad de elementos en espera.
    public int tamano() {
        return tamano;
    }

    /// Indica si la cola no tiene elementos.
    public boolean estaVacia() {
        return tamano == 0;
    }

    /// Vacía la cola.
    public void vaciar() {
        frente = fondo = null;
        tamano = 0;
    }

    /// Recorre desde el frente hasta el final (orden de llegada).
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            /// Nodo que se devolverá a continuación.
            private Nodo<T> actual = frente;

            /// Quedan elementos mientras el nodo actual exista.
            @Override
            public boolean hasNext() {
                return actual != null;
            }

            /// Devuelve el valor actual y avanza hacia el final de la fila.
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
