package co.edu.uq.estructuras.sga.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Tabla hash que asocia claves no nulas con valores.
 *
 * <p>Las colisiones se resuelven mediante encadenamiento separado y la tabla
 * se redimensiona al superar un factor de carga de {@code 0.75}. El iterador
 * recorre los baldes internos; por tanto, su orden no forma parte del
 * contrato.</p>
 *
 * @param <K> tipo de las claves
 * @param <V> tipo de los valores
 */
public class MapaHash<K, V> implements Iterable<MapaHash.Entrada<K, V>> {
    private static final int BUCKET_INICIALES = 16;
    private static final double FACTOR_CARGA_MAXIMO = 0.75;
    private Nodo<K, V>[] baldes = nuevosBuckets(BUCKET_INICIALES);
    private int tamano;

    /**
     * Par inmutable formado por una clave y su valor asociado.
     *
     * @param <K> tipo de la clave
     * @param <V> tipo del valor
     * @param llave clave de la entrada
     * @param valor valor asociado
     */
    public record Entrada<K, V>(K llave, V valor) {

    }

    /** Nodo de una cadena de colisiones dentro de un balde. */
    private static final class Nodo<K, V> {
        /** Clave almacenada. */
        private final K llave;
        /** Valor asociado a la clave. */
        private V valor;
        /** Siguiente nodo del mismo balde. */
        private Nodo<K, V> siguiente;

        /**
         * Crea un nodo enlazado delante de otro.
         *
         * @param llave clave del nodo
         * @param valor valor asociado
         * @param siguiente nodo que quedará después del nuevo nodo
         */
        private Nodo(K llave, V valor, Nodo<K, V> siguiente) {
            this.llave = llave;
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }

    /**
     * Inserta una asociación o reemplaza el valor de una clave existente.
     *
     * @param llave clave no nula
     * @param valor valor que se asociará
     * @return valor anterior, o {@code null} si la clave no existía
     * @throws IllegalArgumentException si {@code llave} es {@code null}
     */
    public V poner(K llave, V valor) {
        int indice = indicePara(llave, baldes.length);
        for (Nodo<K, V> nodo = baldes[indice]; nodo != null; nodo = nodo.siguiente) {
            if (nodo.llave.equals(llave)) {
                V anterior = nodo.valor;
                nodo.valor = valor;
                return anterior;
            }
        }
        baldes[indice] = new Nodo<>(llave, valor, baldes[indice]);
        tamano++;
        if (tamano > baldes.length * FACTOR_CARGA_MAXIMO) {
            redimensionar();
        }
        return null;
    }

    /**
     * Busca el valor asociado a una clave.
     *
     * @param llave clave que se consultará
     * @return valor asociado, o {@code null} si no existe
     * @throws IllegalArgumentException si {@code llave} es {@code null}
     */
    public V obtener(K llave) {
        Nodo<K, V> nodo = buscarNodo(llave);
        return nodo == null ? null : nodo.valor;
    }

    /**
     * Busca el valor asociado a una clave y usa un valor alternativo si falta.
     *
     * @param llave clave que se consultará
     * @param valorPorDefecto valor que se devolverá cuando no exista la clave
     * @return valor asociado o el valor por defecto
     */
    public V obtenerOPorDefecto(K llave, V valorPorDefecto) {
        Nodo<K, V> nodo = buscarNodo(llave);
        return nodo == null ? valorPorDefecto : nodo.valor;
    }

    /**
     * Obtiene el valor existente o crea y almacena uno mediante una fábrica.
     *
     * @param llave clave que se consultará
     * @param fabrica proveedor del valor cuando la clave no existe
     * @return valor existente o valor recién creado
     * @throws IllegalArgumentException si {@code llave} es {@code null}
     */
    public V calcularSiAusente(K llave, Supplier<V> fabrica) {
        Nodo<K, V> nodo = buscarNodo(llave);
        if (nodo != null) {
            return nodo.valor;
        }
        V valor = fabrica.get();
        poner(llave, valor);
        return valor;
    }

    /**
     * Comprueba si existe una asociación para la clave indicada.
     *
     * @param llave clave que se consultará
     * @return {@code true} si la clave está almacenada
     * @throws IllegalArgumentException si {@code llave} es {@code null}
     */
    public boolean contieneLlave(K llave) {
        return buscarNodo(llave) != null;
    }

    /**
     * Elimina una asociación.
     *
     * @param llave clave que se eliminará
     * @return valor eliminado, o {@code null} si la clave no existía
     * @throws IllegalArgumentException si {@code llave} es {@code null}
     */
    public V quitar(K llave) {
        int indice = indicePara(llave, baldes.length);
        Nodo<K, V> anterior = null;
        for (Nodo<K, V> nodo = baldes[indice]; nodo != null; anterior = nodo, nodo = nodo.siguiente) {
            if (nodo.llave.equals(llave)) {
                if (anterior == null) {
                    baldes[indice] = nodo.siguiente;
                } else {
                    anterior.siguiente = nodo.siguiente;
                }
                tamano--;
                return nodo.valor;
            }
        }
        return null;
    }

    /**
     * Copia las claves almacenadas en una lista.
     *
     * @return lista independiente con las claves en el orden del iterador
     */
    public ListaDoblementeEnlazada<K> llaves() {
        ListaDoblementeEnlazada<K> llaves = new ListaDoblementeEnlazada<>();
        for (Entrada<K, V> movimiento : this) {
            llaves.agregarUltimo(movimiento.llave());
        }
        return llaves;
    }

    /**
     * Copia los valores almacenados en una lista.
     *
     * @return lista independiente con los valores en el orden del iterador
     */
    public ListaDoblementeEnlazada<V> valores() {
        ListaDoblementeEnlazada<V> valores = new ListaDoblementeEnlazada<>();
        for (Entrada<K, V> movimiento : this) {
            valores.agregarUltimo(movimiento.valor());
        }
        return valores;
    }

    /**
     * Obtiene la cantidad de asociaciones almacenadas.
     *
     * @return número de claves presentes
     */
    public int tamano() {
        return tamano;
    }

    /**
     * Indica si no hay asociaciones almacenadas.
     *
     * @return {@code true} cuando el mapa está vacío
     */
    public boolean estaVacia() {
        return tamano == 0;
    }

    /**
     * Elimina todas las asociaciones y restablece la capacidad inicial.
     */
    public void vaciar() {
        baldes = nuevosBuckets(BUCKET_INICIALES);
        tamano = 0;
    }

    /**
     * Recorre las asociaciones en el orden de los baldes internos.
     *
     * @return iterador de entradas del mapa
     */
    @Override
    public Iterator<Entrada<K, V>> iterator() {
        return new Iterator<>() {
            /** Balde que se está recorriendo. */
            private int balde = -1;
            /** Nodo que se devolverá a continuación. */
            private Nodo<K, V> actual;

            {
                avanzar();
            }

            /** Avanza dentro del balde actual o hasta el siguiente balde no vacío. */
            private void avanzar() {
                if (actual != null) {
                    actual = actual.siguiente;
                }
                while (actual == null && ++balde < baldes.length) {
                    actual = baldes[balde];
                }
            }

            /** @return {@code true} si queda una entrada por recorrer. */
            @Override
            public boolean hasNext() {
                return actual != null;
            }

            /**
             * Devuelve la siguiente entrada.
             *
             * @return entrada actual
             * @throws NoSuchElementException si no quedan entradas
             */
            @Override
            public Entrada<K, V> next() {
                if (actual == null) {
                    throw new NoSuchElementException();
                }
                Entrada<K, V> movimiento = new Entrada<>(actual.llave, actual.valor);
                avanzar();
                return movimiento;
            }
        };
    }

    private Nodo<K, V> buscarNodo(K llave) {
        for (Nodo<K, V> nodo = baldes[indicePara(llave, baldes.length)]; nodo != null; nodo = nodo.siguiente) {
            if (nodo.llave.equals(llave)) {
                return nodo;
            }
        }
        return null;
    }

    private void redimensionar() {
        Nodo<K, V>[] anterior = baldes;
        baldes = nuevosBuckets(anterior.length * 2);
        for (Nodo<K, V> cabeza : anterior) {
            for (Nodo<K, V> nodo = cabeza; nodo != null; nodo = nodo.siguiente) {
                int indice = indicePara(nodo.llave, baldes.length);
                baldes[indice] = new Nodo<>(nodo.llave, nodo.valor, baldes[indice]);
            }
        }
    }

    private static int indicePara(Object llave, int length) {
        if (llave == null) {
            throw new IllegalArgumentException("MapaHash no admite claves nulas");
        }
        int hash = llave.hashCode();
        hash ^= (hash >>> 16);
        return Math.floorMod(hash, length);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Nodo<K, V>[] nuevosBuckets(int length) {
        return (Nodo<K, V>[]) new Nodo[length];
    }
}
