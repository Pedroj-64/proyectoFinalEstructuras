package co.edu.uq.estructuras.sga.estructuras;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.base.Predicate;

import co.edu.uq.estructuras.sga.excepciones.EstructuraVaciaExcepcion;

/// Cola de prioridad genérica implementada como montículo binario de mínimos sobre un arreglo.
///
/// El orden lo da un [Comparator]: el elemento que compara como "menor" tiene la mayor
/// prioridad. Se usa para la cola de preparación (primero las llegadas de hoy, luego por hora
/// de llegada) y para las reservas pendientes ordenadas por hora de vencimiento.
///
/// `ofrecer` y `sacar` son O(log n); `ver` es O(1).
///
/// @param <T> tipo de elemento
public class ColaPrioridadMonticulo<T> implements Iterable<T> {
    
    /// Capacidad inicial del arreglo.
    private static final int CAPACIDAD_INICIAL = 16;

    /// Define la prioridad: el menor sale primero.
    private final Comparator<? super T> comparador;
    /// Montículo en arreglo: los hijos de `i` están en `2i+1` y `2i+2`; crece al doble cuando se llena.
    private Object[] monticulo = new Object[CAPACIDAD_INICIAL];
    /// Cantidad de elementos.
    private int tamano;

    /// @param comparator define la prioridad: el menor según el comparador sale primero
    public ColaPrioridadMonticulo(Comparator<? super T> comparador) {
        this.comparador = comparador;
    }

    /// Inserta el valor y lo sube hasta su lugar en O(log n).
    public void ofrecer(T valor) {
        if (tamano == monticulo.length) {
            Object[] mayor = new Object[monticulo.length * 2];
            System.arraycopy(monticulo, 0, mayor, 0, tamano);
            monticulo = mayor;
        }
        monticulo[tamano] = valor;
        cribarArriba(tamano);
        tamano++;
    }

    /// Quita y devuelve el elemento de mayor prioridad en O(log n). Lanza [EstructuraVaciaExcepcion] si está vacía.
    public T sacar() {
        if (tamano == 0) {
            throw new EstructuraVaciaExcepcion("PriorityQueue");
        }
        T cima = elementoEn(0);
        tamano--;
        monticulo[0] = monticulo[tamano];
        monticulo[tamano] = null;
        if (tamano > 0) {
            cribarAbajo(0);
        }
        return cima;
    }

    /// Elemento de mayor prioridad sin quitarlo. Lanza [EstructuraVaciaExcepcion] si está vacía.
    public T ver() {
        if (tamano == 0) {
            throw new EstructuraVaciaExcepcion("PriorityQueue");
        }
        return elementoEn(0);
    }

    /// Quita cada elemento que cumple la condición y reconstruye el montículo en O(n).
    public int quitarSi(Predicate<? super T> condicion) {
        int conservado = 0;
        for (int i = 0; i < tamano; i++) {
            if (!condicion.test(elementoEn(i))) {
                monticulo[conservado++] = monticulo[i];
            }
        }
        int quitado = tamano - conservado;
        for (int i = conservado; i < tamano; i++) {
            monticulo[i] = null;
        }
        tamano = conservado;
        monticulizar();
        return quitado;
    }

    /// Indica si algún elemento cumple la condición.
    public boolean matchAny(Predicate<? super T> condicion) {
        for (int i = 0; i < tamano; i++) {
            if (condicion.test(elementoEn(i))) {
                return true;
            }
        }
        return false;
    }

    /// Devuelve los elementos en orden de prioridad sin modificar esta cola.
    public ListaDoblementeEnlazada<T> aListaOrdenada() {
        ColaPrioridadMonticulo<T> copia = new ColaPrioridadMonticulo<>(comparador);
        for (int i = 0; i < tamano; i++) {
            copia.ofrecer(elementoEn(i));
        }
        ListaDoblementeEnlazada<T> ordenados = new ListaDoblementeEnlazada<>();
        while (!copia.estaVacia()) {
            ordenados.agregarUltimo(copia.sacar());
        }
        return ordenados;
    }

    /// Cantidad de elementos.
    public int tamano() {
        return tamano;
    }

    /// Indica si la cola no tiene elementos.
    public boolean estaVacia() {
        return tamano == 0;
    }

    /// Elimina todos los elementos.
    public void vaciar() {
        monticulo = new Object[CAPACIDAD_INICIAL];
        tamano = 0;
    }

    /// Recorre en el orden interno del montículo (no ordenado). Use [#aListaOrdenada()] para el orden de prioridad.
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            /// Posición que se devolverá a continuación.
            private int indice;

            /// Quedan elementos mientras no se haya recorrido todo el arreglo.
            @Override
            public boolean hasNext() {
                return indice < tamano;
            }

            /// Devuelve el siguiente elemento del arreglo (orden del montículo, no de prioridad).
            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elementoEn(indice++);
            }
        };
    }

    /// Reconstruye el montículo en O(n) hundiendo cada nodo interno, desde el último hasta la raíz.
    private void monticulizar() {
        for (int i = tamano / 2 - 1; i >= 0; i--) {
            cribarAbajo(i);
        }
    }

    /// Sube el elemento mientras tenga más prioridad que su padre (después de insertar).
    private void cribarArriba(int indice) {
        while (indice > 0) {
            int padre = (indice - 1) / 2;
            if (compare(indice, padre) >= 0) {
                return;
            }
            intercambiar(indice, padre);
            indice = padre;
        }
    }

    /// Hunde el elemento mientras algún hijo tenga más prioridad (después de sacar la raíz).
    private void cribarAbajo(int indice) {
        while (true) {
            int izquierda = 2 * indice + 1;
            int derecha = izquierda + 1;
            int menor = indice;
            if (izquierda < tamano && compare(izquierda, menor) < 0) {
                menor = izquierda;
            }
            if (derecha < tamano && compare(derecha, menor) < 0) {
                menor = derecha;
            }
            if (menor == indice) {
                return;
            }
            intercambiar(indice, menor);
            indice = menor;
        }
    }

    /// Compara los elementos de dos posiciones con el comparador de la cola.
    private int compare(int i, int j) {
        return comparador.compare(elementoEn(i), elementoEn(j));
    }

    /// Intercambia dos posiciones del arreglo.
    private void intercambiar(int i, int j) {
        Object temporal = monticulo[i];
        monticulo[i] = monticulo[j];
        monticulo[j] = temporal;
    }

    /// Elemento de la posición dada con su tipo genérico.
    @SuppressWarnings("unchecked")
    private T elementoEn(int indice) {
        return (T) monticulo[indice];
    }
}


