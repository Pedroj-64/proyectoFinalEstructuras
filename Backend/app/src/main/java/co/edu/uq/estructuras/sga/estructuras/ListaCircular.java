package co.edu.uq.estructuras.sga.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

import co.edu.uq.estructuras.sga.excepciones.EstructuraVaciaExcepcion;

public class ListaCircular<T> implements Iterable<T> {
    /// Nodo con su valor y el enlace al siguiente (el último apunta al primero).
    private static final class Nodo<T> {
        /// Valor guardado.
        private final T valor;
        /// Siguiente nodo del círculo.
        private Nodo<T> siguiente;

        /// Crea un nodo suelto.
        private Nodo(T valor) {
            this.valor = valor;
        }
    }

    /// Último nodo; `fondo.next` es el primero. Guardar la cola hace que `agregar` sea O(1).
    private Nodo<T> fondo;
    /// Nodo actual de la rotación.
    private Nodo<T> cursor;
    /// Cantidad de elementos.
    private int tamano;

    /// Agrega el valor al final de la vuelta (justo antes del primero).
    public void agregar(T valor) {
        Nodo<T> nodo = new Nodo<>(valor);
        if (fondo == null) {
            nodo.siguiente = nodo;
            cursor = nodo;
        } else {
            nodo.siguiente = fondo.siguiente;
            fondo.siguiente = nodo;
        }
        fondo = nodo;
        tamano++;
    }

    /// Quita el primer elemento igual a `valor`. Devuelve si lo encontró.
    public boolean quitar(T valor) {
        if (fondo == null) {
            return false;
        }
        Nodo<T> anterior = fondo;
        Nodo<T> actual = fondo.siguiente;
        for (int i = 0; i < tamano; i++) {
            if (actual.valor.equals(valor)) {
                if (tamano == 1) {
                    fondo = cursor = null;
                } else {
                    anterior.siguiente = actual.siguiente;
                    if (actual == fondo) {
                        fondo = anterior;
                    }
                    if (actual == cursor) {
                        cursor = actual.siguiente;
                    }
                }
                tamano--;
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    /// Elemento bajo el cursor.
    public T actual() {
        if (cursor == null) {
            throw new EstructuraVaciaExcepcion("ListaCircular");
        }
        return cursor.valor;
    }

    /// Mueve el cursor una posición y devuelve el nuevo elemento actual.
    public T avanzar() {
        if (cursor == null) {
            throw new EstructuraVaciaExcepcion("ListaCircular");
        }
        cursor = cursor.siguiente;
        return cursor.valor;
    }

    /// Elemento que está `pasos` posiciones después del primero, dando las vueltas que haga falta.
    public T obtenerRotado(long pasos) {
        if (fondo == null) {
            throw new EstructuraVaciaExcepcion("ListaCircular");
        }
        int desfase = (int) Math.floorMod(pasos, (long) tamano);
        Nodo<T> actual = fondo.siguiente;
        for (int i = 0; i < desfase; i++) {
            actual = actual.siguiente;
        }
        return actual.valor;
    }

    /// Cantidad de elementos de la lista.
    public int tamano() {
        return tamano;
    }

    /// Indica si la lista no tiene elementos.
    public boolean estaVacia() {
        return tamano == 0;
    }

    /// Elimina todos los elementos.
    public void vaciar() {
        fondo = cursor = null;
        tamano = 0;
    }

    /// Recorre exactamente una vuelta, empezando por el primer elemento insertado.
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            /// Nodo que se devolverá a continuación.
            private Nodo<T> actual = fondo == null ? null : fondo.siguiente;
            /// Cuántos se han devuelto (para detenerse al completar la vuelta).
            private int visitados;

            /// Quedan elementos mientras no se haya completado la vuelta.
            @Override
            public boolean hasNext() {
                return visitados < tamano;
            }

            /// Devuelve el elemento actual y avanza al siguiente nodo del círculo.
            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T valor = actual.valor;
                actual = actual.siguiente;
                visitados++;
                return valor;
            }
        };
    }
    
}
