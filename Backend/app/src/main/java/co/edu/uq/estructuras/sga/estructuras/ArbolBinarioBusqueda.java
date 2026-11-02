package co.edu.uq.estructuras.sga.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.ToIntFunction;

import co.edu.uq.estructuras.sga.excepciones.EstructuraVaciaExcepcion;

/**
 * Árbol binario de búsqueda sin duplicados, ordenado por
 * {@link Comparable#compareTo(Object)}.
 *
 * <p>Las búsquedas guiadas pueden podar subárboles mediante
 * {@link GuiaBusqueda}. Los recorridos devuelven copias en listas y el
 * iterador produce los valores en orden ascendente.</p>
 *
 * @param <T> tipo comparable almacenado en el árbol
 */
public class ArbolBinarioBusqueda<T extends Comparable<T>> implements Iterable<T> {

    private Nodo<T> raiz;
    private int tamano;

    /** Nodo con un valor y sus dos posibles subárboles. */
    private static final class Nodo<T> {
        private T valor;
        private Nodo<T> izquierda;
        private Nodo<T> derecha;

        /**
         * Crea un nodo hoja.
         *
         * @param valor valor almacenado
         */
        private Nodo(T valor) {
            this.valor = valor;
        }
    }

    /**
     * Inserta un valor si no existe otro equivalente.
     *
     * @param valor valor que se insertará
     * @return {@code true} si se insertó; {@code false} si ya existía
     */
    public boolean insertar(T valor) {
        int antes = tamano;
        raiz = insertar(raiz, valor);
        return tamano > antes;
    }

    private Nodo<T> insertar(Nodo<T> nodo, T valor) {
        if (nodo == null) {
            tamano++;
            return new Nodo<>(valor);
        }
        int comparacion = valor.compareTo(nodo.valor);
        if (comparacion < 0) {
            nodo.izquierda = insertar(nodo.izquierda, valor);
        } else if (comparacion > 0) {
            nodo.derecha = insertar(nodo.derecha, valor);
        }
        return nodo;
    }

    /**
     * Busca un valor usando su orden natural.
     *
     * @param sonda valor que se buscará
     * @return valor almacenado equivalente, o {@code null} si no existe
     */
    public T buscar(T sonda) {
        return buscarPor(sonda::compareTo);
    }

    /**
     * Busca un elemento mediante una función de comparación contra la clave.
     *
     * <p>La función recibe el valor almacenado y debe devolver un número
     * negativo, cero o positivo cuando la clave buscada es menor, igual o
     * mayor que dicho valor, respectivamente.</p>
     *
     * @param comparacionLlave comparación entre la clave buscada y un valor
     *                         almacenado
     * @return valor coincidente, o {@code null} si no existe
     */
    public T buscarPor(ToIntFunction<T> comparacionLlave) {
        return buscarPor(raiz, comparacionLlave);
    }

    /** Busca recursivamente a partir del nodo indicado. */
    private T buscarPor(Nodo<T> nodo, ToIntFunction<T> comparacionLlave) {
        if (nodo == null) {
            return null;
        }
        int comparacion = comparacionLlave.applyAsInt(nodo.valor);
        if (comparacion == 0) {
            return nodo.valor;
        }
        return comparacion < 0 ? buscarPor(nodo.izquierda, comparacionLlave)
                : buscarPor(nodo.derecha, comparacionLlave);
    }

    /**
     * Comprueba si existe un valor equivalente a la sonda.
     *
     * @param sonda valor que se consultará
     * @return {@code true} si el árbol contiene un valor equivalente
     */
    public boolean contiene(T sonda) {
        return buscar(sonda) != null;
    }

    /**
     * Elimina un valor y reorganiza el subárbol afectado.
     *
     * @param valor valor que se eliminará
     * @return {@code true} si se eliminó un valor
     */
    public boolean eliminar(T valor) {
        int antes = tamano;
        raiz = eliminar(raiz, valor);
        return tamano < antes;
    }

    private Nodo<T> eliminar(Nodo<T> nodo, T valor) {
        if (nodo == null) {
            return null;
        }
        int comparacion = valor.compareTo(nodo.valor);
        if (comparacion < 0) {
            nodo.izquierda = eliminar(nodo.izquierda, valor);
            return nodo;
        }
        if (comparacion > 0) {
            nodo.derecha = eliminar(nodo.derecha, valor);
            return nodo;
        }
        if (nodo.izquierda == null) {
            tamano--;
            return nodo.derecha;
        }
        if (nodo.derecha == null) {
            tamano--;
            return nodo.izquierda;
        }
        // Con dos hijos, se reemplaza por el sucesor en inorden.
        Nodo<T> sucesor = nodoMinimo(nodo.derecha);
        nodo.valor = sucesor.valor;
        nodo.derecha = eliminar(nodo.derecha, sucesor.valor);
        return nodo;
    }

    /**
     * Devuelve la primera coincidencia en recorrido en orden, aplicando la
     * estrategia de exploración indicada.
     *
     * @param guia reglas de coincidencia y poda de subárboles
     * @return primera coincidencia, o {@code null} si no existe
     */
    public T buscarPrimero(GuiaBusqueda<T> guia) {
        return buscarPrimero(raiz, guia);
    }

    private T buscarPrimero(Nodo<T> nodo, GuiaBusqueda<T> guia) {
        if (nodo == null) {
            return null;
        }
        if (guia.explorarIzquierda(nodo.valor)) {
            T desdeIzquierda = buscarPrimero(nodo.izquierda, guia);
            if (desdeIzquierda != null) {
                return desdeIzquierda;
            }
        }
        if (guia.coincide(nodo.valor)) {
            return nodo.valor;
        }
        return guia.explorarDerecha(nodo.valor) ? buscarPrimero(nodo.derecha, guia) : null;
    }

    /**
     * Obtiene todas las coincidencias en orden ascendente.
     *
     * @param guia reglas de coincidencia y poda de subárboles
     * @return lista independiente con los valores coincidentes
     */
    public ListaDoblementeEnlazada<T> buscarTodos(GuiaBusqueda<T> guia) {
        ListaDoblementeEnlazada<T> resultado = new ListaDoblementeEnlazada<>();
        buscarTodos(raiz, guia, resultado);
        return resultado;
    }

    private void buscarTodos(Nodo<T> nodo, GuiaBusqueda<T> guia, ListaDoblementeEnlazada<T> resultado) {
        if (nodo == null) {
            return;
        }
        if (guia.explorarIzquierda(nodo.valor)) {
            buscarTodos(nodo.izquierda, guia, resultado);
        }
        if (guia.coincide(nodo.valor)) {
            resultado.agregarUltimo(nodo.valor);
        }
        if (guia.explorarDerecha(nodo.valor)) {
            buscarTodos(nodo.derecha, guia, resultado);
        }
    }

    /**
     * Calcula la altura del árbol contando niveles; un árbol vacío tiene
     * altura cero.
     *
     * @return altura del árbol
     */
    public int altura() {
        return altura(raiz);
    }

    private int altura(Nodo<T> nodo) {
        if (nodo == null) {
            return 0;
        }
        return 1 + Math.max(altura(nodo.izquierda), altura(nodo.derecha));
    }

    /**
     * Obtiene el menor valor almacenado.
     *
     * @return valor mínimo
     * @throws EstructuraVaciaExcepcion si el árbol está vacío
     */
    public T minimo() {
        if (raiz == null) {
            throw new EstructuraVaciaExcepcion("ArbolBinarioBusqueda");
        }
        return nodoMinimo(raiz).valor;
    }

    /**
     * Obtiene el mayor valor almacenado.
     *
     * @return valor máximo
     * @throws EstructuraVaciaExcepcion si el árbol está vacío
     */
    public T maximo() {
        if (raiz == null) {
            throw new EstructuraVaciaExcepcion("ArbolBinarioBusqueda");
        }
        return nodoMaximo(raiz).valor;
    }

    private Nodo<T> nodoMinimo(Nodo<T> nodo) {
        return nodo.izquierda == null ? nodo : nodoMinimo(nodo.izquierda);
    }

    private Nodo<T> nodoMaximo(Nodo<T> nodo) {
        return nodo.derecha == null ? nodo : nodoMaximo(nodo.derecha);
    }

    /**
     * Recorre izquierda, raíz y derecha.
     *
     * @return valores en orden ascendente
     */
    public ListaDoblementeEnlazada<T> enOrden() {
        ListaDoblementeEnlazada<T> resultado = new ListaDoblementeEnlazada<>();
        enOrden(raiz, resultado);
        return resultado;
    }

    private void enOrden(Nodo<T> nodo, ListaDoblementeEnlazada<T> resultado) {
        if (nodo != null) {
            enOrden(nodo.izquierda, resultado);
            resultado.agregarUltimo(nodo.valor);
            enOrden(nodo.derecha, resultado);
        }
    }

    /**
     * Recorre raíz, izquierda y derecha.
     *
     * @return valores en recorrido preorden
     */
    public ListaDoblementeEnlazada<T> preOrden() {
        ListaDoblementeEnlazada<T> resultado = new ListaDoblementeEnlazada<>();
        preOrden(raiz, resultado);
        return resultado;
    }

    private void preOrden(Nodo<T> nodo, ListaDoblementeEnlazada<T> resultado) {
        if (nodo != null) {
            resultado.agregarUltimo(nodo.valor);
            preOrden(nodo.izquierda, resultado);
            preOrden(nodo.derecha, resultado);
        }
    }

    /**
     * Recorre izquierda, derecha y raíz.
     *
     * @return valores en recorrido postorden
     */
    public ListaDoblementeEnlazada<T> postOrden() {
        ListaDoblementeEnlazada<T> resultado = new ListaDoblementeEnlazada<>();
        postOrden(raiz, resultado);
        return resultado;
    }

    private void postOrden(Nodo<T> nodo, ListaDoblementeEnlazada<T> resultado) {
        if (nodo != null) {
            postOrden(nodo.izquierda, resultado);
            postOrden(nodo.derecha, resultado);
            resultado.agregarUltimo(nodo.valor);
        }
    }

    /**
     * Obtiene la cantidad de valores almacenados.
     *
     * @return número de valores del árbol
     */
    public int tamano() {
        return tamano;
    }

    /**
     * Indica si el árbol no contiene valores.
     *
     * @return {@code true} si está vacío
     */
    public boolean estaVacia() {
        return tamano == 0;
    }

    /** Elimina todos los valores del árbol. */
    public void vaciar() {
        raiz = null;
        tamano = 0;
    }

    /**
     * Recorre los valores en orden ascendente.
     *
     * @return iterador inorden
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            /** Nodos pendientes de visitar. */
            private final PilaEnlazada<Nodo<T>> pendiente = new PilaEnlazada<>();

            {
                apilarRamaIzquierda(raiz);
            }

            /** Apila un nodo y toda su rama izquierda. */
            private void apilarRamaIzquierda(Nodo<T> nodo) {
                for (Nodo<T> actual = nodo; actual != null; actual = actual.izquierda) {
                    pendiente.apilar(actual);
                }
            }

            /** @return {@code true} si quedan nodos por visitar. */
            @Override
            public boolean hasNext() {
                return !pendiente.estaVacia();
            }

            /**
             * Devuelve el siguiente valor en orden ascendente.
             *
             * @return siguiente valor
             * @throws NoSuchElementException si no quedan valores
             */
            @Override
            public T next() {
                if (pendiente.estaVacia()) {
                    throw new NoSuchElementException();
                }
                Nodo<T> nodo = pendiente.desapilar();
                apilarRamaIzquierda(nodo.derecha);
                return nodo.valor;
            }
        };
    }
}
