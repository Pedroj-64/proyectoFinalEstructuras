package co.edu.uq.estructuras.sga.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

import co.edu.uq.estructuras.sga.excepciones.EstructuraVaciaExcepcion;

/**
 * Lista enlazada circular con un cursor mutable.
 *
 * <p>El primer elemento es el sucesor de {@code fondo}. El cursor permite
 * recorrer la lista circularmente sin exponer los nodos internos.</p>
 *
 * @param <T> tipo de los elementos almacenados
 */
public class ListaCircular<T> implements Iterable<T> {
    private Nodo<T> fondo;
    private Nodo<T> cursor;
    private int tamano;

    /** Nodo que ocupa la posición final y enlaza con el primero. */
    private static final class Nodo<T> {
        private final T valor;
        private Nodo<T> siguiente;

        /** Crea un nodo sin sucesor. */
        private Nodo(T valor) {
            this.valor = valor;
        }
    }

   
    /**
     * Agrega un valor al final de la vuelta, justo antes del primero.
     *
     * @param valor elemento que se agregará
     */
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

    /**
     * Elimina la primera coincidencia según {@link Object#equals(Object)}.
     *
     * @param valor valor que se buscará
     * @return {@code true} si se eliminó un elemento
     */
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

    /**
     * Obtiene el elemento situado bajo el cursor.
     *
     * @return elemento actual
     * @throws EstructuraVaciaExcepcion si la lista está vacía
     */
    public T actual() {
        if (cursor == null) {
            throw new EstructuraVaciaExcepcion("ListaCircular");
        }
        return cursor.valor;
    }

    /**
     * Avanza el cursor una posición.
     *
     * @return nuevo elemento actual
     * @throws EstructuraVaciaExcepcion si la lista está vacía
     */
    public T avanzar() {
        if (cursor == null) {
            throw new EstructuraVaciaExcepcion("ListaCircular");
        }
        cursor = cursor.siguiente;
        return cursor.valor;
    }

    /**
     * Obtiene el elemento situado un número de pasos después del primero.
     *
     * <p>Los pasos se normalizan mediante aritmética modular, por lo que
     * también se admiten valores negativos o mayores que el tamaño.</p>
     *
     * @param pasos desplazamiento desde el primer elemento
     * @return elemento situado en la posición rotada
     * @throws EstructuraVaciaExcepcion si la lista está vacía
     */
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

    /**
     * Obtiene la cantidad de elementos.
     *
     * @return número de elementos almacenados
     */
    public int tamano() {
        return tamano;
    }

    /**
     * Indica si la lista está vacía.
     *
     * @return {@code true} si no contiene elementos
     */
    public boolean estaVacia() {
        return tamano == 0;
    }

    /** Elimina todos los elementos y reinicia el cursor. */
    public void vaciar() {
        fondo = cursor = null;
        tamano = 0;
    }

    /**
     * Recorre exactamente una vuelta desde el primer elemento.
     *
     * @return iterador que produce cada elemento una vez
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            /** Nodo que se devolverá a continuación. */
            private Nodo<T> actual = fondo == null ? null : fondo.siguiente;
            /** Cantidad de elementos ya devueltos en la vuelta actual. */
            private int visitados;

            /** @return {@code true} si aún no se completó la vuelta. */
            @Override
            public boolean hasNext() {
                return visitados < tamano;
            }

            /**
             * Devuelve el elemento actual y avanza al siguiente nodo.
             *
             * @return valor actual
             * @throws NoSuchElementException si se completó la vuelta
             */
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
