package co.edu.uq.estructuras.sga.estructuras;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import co.edu.uq.estructuras.sga.excepciones.EstructuraVaciaExcepcion;

/**
 * Clase que representa una lista doblemente enlazada.
 * 
 * Se usa para: el historial de estancias de cada huésped, los ocupantes de una
 * reserva y el
 * desglose noche por noche de una cotización. Insertar y quitar en ambos
 * extremos es O(1); el
 * acceso por posición camina desde el extremo más cercano.
 */
public class ListaDoblementeEnlazada<T> implements Iterable<T> {
    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamano;

    /**
     * Nodo interno que almacena un valor y sus enlaces hacia los nodos
     * adyacentes.
     *
     * @param <T> tipo del valor almacenado
     */
    private static final class Nodo<T> {
        private T valor;
        private Nodo<T> anterior;
        private Nodo<T> siguiente;

        /**
         * Crea un nodo aislado con el valor indicado.
         *
         * @param valor valor que almacenará el nodo
         */
        private Nodo(T valor) {
            this.valor = valor;
        }
    }

    /**
     * Crea una lista nueva con los elementos de otra fuente iterable, en el
     * mismo orden. La copia es estructural: los elementos no se clonan.
     *
     * @param <T>   tipo de los elementos
     * @param lista fuente cuyos elementos se copiarán
     * @return una lista doblemente enlazada independiente
     */
    public static <T> ListaDoblementeEnlazada<T> copiarDe(Iterable<? extends T> lista) {
        ListaDoblementeEnlazada<T> listaNueva = new ListaDoblementeEnlazada<>();
        for (T valor : lista) {
            listaNueva.agregarUltimo(valor);
        }
        return listaNueva;
    }

    /**
     * Inserta un elemento al principio de la lista.
     *
     * @param valor elemento que se agregará
     */
    public void agregarPrimero(T valor) {
        Nodo<T> nodo = new Nodo<>(valor);
        if (cabeza == null) {
            cabeza = cola = nodo;
        } else {
            nodo.siguiente = cabeza;
            cabeza.anterior = nodo;
            cabeza = nodo;

        }
        tamano++;

    }

    /**
     * Inserta un elemento al final de la lista.
     *
     * @param valor elemento que se agregará
     */
    public void agregarUltimo(T valor) {
        Nodo<T> nodo = new Nodo<>(valor);
        if (cola == null) {
            cabeza = cola = nodo;
        } else {
            nodo.anterior = cola;
            cola.siguiente = nodo;
            cola = nodo;
        }
        tamano++;
    }

    /**
     * Agrega al final todos los elementos de una fuente iterable, conservando
     * su orden de recorrido.
     *
     * @param lista fuente de elementos que se agregarán
     */
    public void agregarTodo(Iterable<? extends T> lista) {
        for (T valor : lista) {
            agregarUltimo(valor);
        }
    }

    /**
     * Elimina y devuelve el primer elemento.
     *
     * @return valor almacenado en el primer nodo
     * @throws EstructuraVaciaExcepcion si la lista no contiene elementos
     */
    public T quitarPrimero() {
        if (cabeza == null) {
            throw new EstructuraVaciaExcepcion("ListaDoblementeEnlazada");
        }
        Nodo<T> quitado = cabeza;
        desenlazar(quitado);
        return quitado.valor;
    }

    /**
     * Elimina y devuelve el último elemento.
     *
     * @return valor almacenado en el último nodo
     * @throws EstructuraVaciaExcepcion si la lista no contiene elementos
     */
    public T quitarUltimo() {
        if (cola == null) {
            throw new EstructuraVaciaExcepcion("ListaDoblementeEnlazada");
        }
        Nodo<T> quitado = cola;
        desenlazar(quitado);
        return quitado.valor;
    }

    /**
     * Elimina la primera aparición cuyo valor sea la misma referencia que el
     * valor recibido.
     *
     * <p>
     * La comparación se realiza con {@code ==}, no con
     * {@code equals(Object)}.
     * </p>
     *
     * @param valor referencia que se buscará
     * @return {@code true} si se eliminó un elemento; {@code false} si no se
     *         encontró
     */
    public boolean quitar(T valor) {
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente) {
            if (actual.valor == valor) {
                desenlazar(actual);
                return true;
            }
        }
        return false;
    }

    /**
     * Elimina todos los elementos que satisfacen una condición.
     *
     * @param condicion predicado que determina qué elementos eliminar
     * @return cantidad de elementos eliminados
     */
    public int quitarSi(Predicate<? super T> condicion) {
        int quitados = 0;
        Nodo<T> actual = cabeza;
        while (actual != null) {
            Nodo<T> siguiente = actual.siguiente;
            if (condicion.test(actual.valor)) {
                desenlazar(actual);
                quitados++;
            }
            actual = siguiente;
        }
        return quitados;
    }

    /**
     * Obtiene el elemento situado en una posición.
     *
     * @param indice índice basado en cero
     * @return elemento ubicado en el índice
     * @throws IndexOutOfBoundsException si el índice no pertenece a la lista
     */
    public T obtener(int indice) {
        return nodoEn(indice).valor;
    }

    /**
     * Reemplaza el valor situado en una posición.
     *
     * @param indice índice basado en cero
     * @param valor  nuevo valor
     * @throws IndexOutOfBoundsException si el índice no pertenece a la lista
     */
    public void establecer(int indice, T valor) {
        nodoEn(indice).valor = valor;
    }

    /**
     * Obtiene el primer elemento de la lista.
     *
     * @return valor almacenado en el primer nodo
     * @throws EstructuraVaciaExcepcion si la lista no contiene elementos
     */
    public T obtenerPrimero() {
        if (cabeza == null) {
            throw new EstructuraVaciaExcepcion("ListaDoblementeEnlazada");
        }
        return cabeza.valor;
    }

    /**
     * Busca la primera posición cuyo elemento sea la misma referencia que el
     * valor recibido.
     *
     * <p>
     * La comparación se realiza con {@code ==}, no con
     * {@code equals(Object)}.
     * </p>
     *
     * @param valor referencia que se buscará
     * @return índice de la primera coincidencia o {@code -1} si no existe
     */
    public int indexOf(T valor) {
        int indice = 0;
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente, indice++) {
            if (actual.valor == valor) {
                return indice;
            }
        }
        return -1;
    }

    /**
     * Devuelve el primer elemento que satisface una condición.
     *
     * @param condicion predicado que se evaluará en cada elemento
     * @return primer elemento coincidente o {@code null} si no existe
     */
    public T buscar(Predicate<? super T> condicion) {
        for (T valor : this) {
            if (condicion.test(valor)) {
                return valor;
            }
        }
        return null;
    }

    /**
     * Cuenta cuántos elementos satisfacen una condición.
     *
     * @param condicion predicado que se evaluará en cada elemento
     * @return cantidad de elementos coincidentes
     */
    public int cantidadSiCumplen(Predicate<? super T> condicion) {
        int total = 0;
        for (T valor : this) {
            if (condicion.test(valor)) {
                total++;
            }
        }
        return total;
    }

    /**
     * Crea una lista nueva con los elementos que satisfacen una condición, en
     * el mismo orden de la lista original.
     *
     * @param condicion predicado que determina qué elementos incluir
     * @return lista filtrada independiente
     */
    public ListaDoblementeEnlazada<T> filtrar(Predicate<? super T> condicion) {
        ListaDoblementeEnlazada<T> resultado = new ListaDoblementeEnlazada<>();
        for (T valor : this) {
            if (condicion.test(valor)) {
                resultado.agregarUltimo(valor);
            }
        }
        return resultado;
    }

    /**
     * Crea una copia ordenada sin modificar esta lista.
     *
     * <p>
     * Utiliza merge sort y conserva el orden relativo de elementos iguales.
     * La complejidad temporal es O(n log n).
     * </p>
     *
     * @param comparador criterio de ordenamiento
     * @return nueva lista con los elementos ordenados
     */
    public ListaDoblementeEnlazada<T> ordenada(Comparator<? super T> comparador) {
        ListaDoblementeEnlazada<T> copia = copiarDe(this);
        copia.cabeza = mezclarOrdenar(copia.cabeza, comparador);
        Nodo<T> ultimo = copia.cabeza;
        if (ultimo != null) {
            ultimo.anterior = null;
            while (ultimo.siguiente != null) {
                ultimo.siguiente.anterior = ultimo;
                ultimo = ultimo.siguiente;
            }
        }
        copia.cola = ultimo;
        return copia;
    }

    /**
     * Copia los elementos a un arreglo creado por el generador recibido.
     *
     * @param generador función que crea un arreglo del tamaño necesario
     * @return arreglo con los elementos en el orden de la lista
     */
    public T[] aArreglo(IntFunction<T[]> generador) {
        T[] arreglo = generador.apply(tamano);
        int indice = 0;
        for (T valor : this) {
            arreglo[indice++] = valor;
        }
        return arreglo;
    }

    /**
     * Elimina todos los elementos de la lista.
     */
    public void vaciar() {
        cabeza = cola = null;
        tamano = 0;
    }

    /**
     * Obtiene la cantidad actual de elementos.
     *
     * @return número de elementos almacenados
     */
    public int tamano() {
        return tamano;
    }

    /**
     * Indica si la lista no contiene elementos.
     *
     * @return {@code true} cuando la lista está vacía
     */
    public boolean estaVacia() {
        return tamano == 0;
    }

    /**
     * Ordena una cadena de nodos mediante merge sort y devuelve su nueva
     * cabeza. Este método modifica únicamente los enlaces {@code siguiente};
     * los enlaces inversos se reconstruyen al finalizar el ordenamiento.
     *
     * @param inicio     primer nodo de la cadena a ordenar
     * @param comparador criterio de ordenamiento
     * @param <T>        tipo de los valores de los nodos
     * @return cabeza de la cadena ordenada
     */
    private static <T> Nodo<T> mezclarOrdenar(Nodo<T> inicio, Comparator<? super T> comparador) {
        if (inicio == null || inicio.siguiente == null) {
            return inicio;
        }
        Nodo<T> medio = medioDe(inicio);
        Nodo<T> segundaMitad = medio.siguiente;
        medio.siguiente = null;
        return mezclar(mezclarOrdenar(inicio, comparador), mezclarOrdenar(segundaMitad, comparador), comparador);
    }

    /**
     * Encuentra el nodo medio usando dos punteros: uno avanza una posición y el
     * otro avanza dos posiciones por iteración.
     *
     * @param inicio primer nodo de la cadena
     * @param <T>    tipo de los valores de los nodos
     * @return último nodo de la primera mitad
     */
    private static <T> Nodo<T> medioDe(Nodo<T> inicio) {
        Nodo<T> lento = inicio;
        Nodo<T> rapido = inicio.siguiente;
        while (rapido != null && rapido.siguiente != null) {
            lento = lento.siguiente;
            rapido = rapido.siguiente.siguiente;
        }
        return lento;
    }

    /**
     * Fusiona dos cadenas ordenadas en una sola, reutilizando sus nodos.
     *
     * @param izquierda  cabeza de la primera cadena ordenada
     * @param derecha    cabeza de la segunda cadena ordenada
     * @param comparador criterio de ordenamiento
     * @param <T>        tipo de los valores de los nodos
     * @return cabeza de la cadena fusionada
     */
    private static <T> Nodo<T> mezclar(Nodo<T> izquierda, Nodo<T> derecha, Comparator<? super T> comparador) {
        Nodo<T> centinela = new Nodo<>(null);
        Nodo<T> ultimo = centinela;
        while (izquierda != null && derecha != null) {
            if (comparador.compare(izquierda.valor, derecha.valor) <= 0) {
                ultimo.siguiente = izquierda;
                izquierda = izquierda.siguiente;
            } else {
                ultimo.siguiente = derecha;
                derecha = derecha.siguiente;
            }
            ultimo = ultimo.siguiente;
        }
        ultimo.siguiente = izquierda != null ? izquierda : derecha;
        return centinela.siguiente;
    }

    /**
     * Indica si al menos un elemento satisface una condición.
     *
     * @param condicion predicado que se evaluará en cada elemento
     * @return {@code true} si existe una coincidencia; {@code false} en caso
     *         contrario
     */
    public boolean matchAny(Predicate<? super T> condicion) {
        return buscar(condicion) != null;
    }

    /**
     * Indica si la lista contiene la misma referencia que el valor recibido.
     *
     * @param valor referencia que se buscará
     * @return {@code true} si se encuentra; {@code false} en caso contrario
     */
    public boolean contiene(T valor) {
        return indexOf(valor) >= 0;
    }

    /**
     * Obtiene el último elemento de la lista.
     *
     * @return valor almacenado en el último nodo
     * @throws EstructuraVaciaExcepcion si la lista no contiene elementos
     */
    public T obtenerUltimo() {
        if (cola == null) {
            throw new EstructuraVaciaExcepcion("ListaDoblementeEnlazada");
        }
        return cola.valor;
    }

    /**
     * Localiza el nodo de una posición recorriendo desde el extremo más
     * cercano: la cabeza para índices iniciales y la cola para índices finales.
     *
     * @param indice índice basado en cero
     * @return nodo situado en el índice indicado
     * @throws IndexOutOfBoundsException si el índice no pertenece a la lista
     */
    private Nodo<T> nodoEn(int indice) {
        if (indice < 0 || indice >= tamano) {
            throw new IndexOutOfBoundsException("Indice fuera de rango");
        }
        if (indice < tamano / 2) {
            Nodo<T> actual = cabeza;
            for (int i = 0; i < indice; i++) {
                actual = actual.siguiente;
            }
            return actual;
        }
        Nodo<T> actual = cola;
        for (int i = tamano - 1; i > indice; i--) {
            actual = actual.anterior;
        }
        return actual;

    }

    /**
     * Desenlaza un nodo de la lista y actualiza sus extremos y tamaño.
     *
     * @param nodo nodo perteneciente a esta lista que se eliminará
     */
    private void desenlazar(Nodo<T> nodo) {
        if (nodo.anterior == null) {
            cabeza = nodo.siguiente;
        } else {
            nodo.anterior.siguiente = nodo.siguiente;
        }
        if (nodo.siguiente == null) {
            cola = nodo.anterior;
        } else {
            nodo.siguiente.anterior = nodo.anterior;
        }
        nodo.siguiente = nodo.anterior = null;
        tamano--;
    }

    /**
     * Crea un iterador que recorre la lista desde la cabeza hasta la cola.
     *
     * @return iterador de recorrido hacia adelante
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            /** Nodo que se devolverá a continuación. */
            private Nodo<T> actual = cabeza;

            /** Indica si queda un nodo por recorrer. */
            @Override
            public boolean hasNext() {
                return actual != null;
            }

            /** Devuelve el valor actual y avanza al siguiente nodo. */
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

    /**
     * Crea una vista iterable que recorre la lista desde la cola hasta la
     * cabeza. La vista refleja los nodos existentes cuando se consume.
     *
     * @return iterable de recorrido hacia atrás
     */
    public Iterable<T> reversa() {
        return () -> new Iterator<>() {
            /** Nodo que se devolverá a continuación. */
            private Nodo<T> actual = cola;

            /** Indica si queda un nodo por recorrer. */
            @Override
            public boolean hasNext() {
                return actual != null;
            }

            /** Devuelve el valor actual y retrocede al nodo anterior. */
            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException();
                }
                T valor = actual.valor;
                actual = actual.anterior;
                return valor;
            }
        };
    }

    /**
     * Representa la lista como una secuencia de valores entre corchetes,
     * separados por coma y espacio.
     *
     * @return representación textual de la lista
     */
    @Override
    public String toString() {
        StringBuilder constructor = new StringBuilder("[");
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente) {
            constructor.append(actual.valor);
            if (actual.siguiente != null) {
                constructor.append(", ");
            }
        }
        return constructor.append(']').toString();
    }
}
