package co.edu.uq.estructuras.sga.estructuras;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.base.Predicate;

import co.edu.uq.estructuras.sga.excepciones.EstructuraVaciaExcepcion;

/**
 * Cola de prioridad genérica implementada como un montículo binario de
 * mínimos sobre un arreglo dinámico.
 *
 * <p>El comparador define la prioridad: el elemento que compara como menor
 * ocupa la raíz y se devuelve primero. Se utiliza para la cola de preparación
 * (primero las llegadas de hoy y luego la hora de llegada) y para las reservas
 * pendientes ordenadas por hora de vencimiento.</p>
 *
 * <p>{@link #ofrecer(Object)} y {@link #sacar()} tienen complejidad O(log n);
 * {@link #ver()} tiene complejidad O(1).</p>
 *
 * @param <T> tipo de los elementos almacenados
 */
public class ColaPrioridadMonticulo<T> implements Iterable<T> {
    /** Capacidad inicial del arreglo interno. */
    private static final int CAPACIDAD_INICIAL = 16;

    /** Criterio que determina la prioridad relativa de dos elementos. */
    private final Comparator<? super T> comparador;

    /** Arreglo que representa el montículo en orden por niveles. */
    private Object[] monticulo = new Object[CAPACIDAD_INICIAL];

    /** Cantidad de posiciones ocupadas en el arreglo interno. */
    private int tamano;

    /**
     * Crea una cola de prioridad vacía.
     *
     * @param comparador criterio de prioridad; el menor elemento según este
     *                   comparador se extrae primero
     */
    public ColaPrioridadMonticulo(Comparator<? super T> comparador) {
       this.comparador = comparador;
    }

    /**
    * Inserta un elemento y lo desplaza hacia arriba hasta restaurar la
    * propiedad del min-heap.
    *
    * <p>Si el arreglo se llena, su capacidad se duplica antes de insertar.</p>
    *
    * @param valor elemento que se agregará
    */
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

    /**
     * Retira y devuelve el elemento de mayor prioridad.
     *
     * <p>La raíz se reemplaza por el último elemento y este se desplaza hacia
     * abajo para conservar la propiedad del montículo.</p>
     *
     * @return elemento menor según el comparador
     * @throws EstructuraVaciaExcepcion si la cola no contiene elementos
     */
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

    /**
     * Consulta el elemento de mayor prioridad sin retirarlo.
     *
     * @return elemento menor según el comparador
     * @throws EstructuraVaciaExcepcion si la cola no contiene elementos
     */
    public T ver() {
        if (tamano == 0) {
            throw new EstructuraVaciaExcepcion("PriorityQueue");
        }
        return elementoEn(0);
    }

    /**
     * Elimina todos los elementos que satisfacen una condición y reconstruye
     * el montículo con los elementos restantes.
     *
     * @param condicion predicado que determina qué elementos se eliminarán
     * @return cantidad de elementos eliminados
     */
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

    /**
     * Indica si al menos un elemento satisface una condición.
     *
     * @param condicion predicado que se evaluará sobre los elementos
     * @return {@code true} si existe una coincidencia; {@code false} en caso
     *         contrario
     */
    public boolean matchAny(Predicate<? super T> condicion) {
        for (int i = 0; i < tamano; i++) {
            if (condicion.test(elementoEn(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtiene los elementos en orden de prioridad sin modificar esta cola.
     *
     * <p>La operación trabaja sobre una copia y devuelve una lista que
     * comienza con el elemento de mayor prioridad.</p>
     *
     * @return lista independiente con los elementos ordenados por prioridad
     */
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

    /**
     * Obtiene la cantidad actual de elementos.
     *
     * @return número de elementos almacenados
     */
    public int tamano() {
        return tamano;
    }

    /**
     * Indica si la cola no contiene elementos.
     *
     * @return {@code true} si está vacía; {@code false} en caso contrario
     */
    public boolean estaVacia() {
        return tamano == 0;
    }

    /**
     * Elimina todos los elementos y restaura la capacidad inicial del arreglo.
     */
    public void vaciar() {
        monticulo = new Object[CAPACIDAD_INICIAL];
        tamano = 0;
    }

    /**
     * Crea un iterador sobre el orden interno del arreglo del montículo.
     *
     * <p>Este recorrido no está ordenado por prioridad; para obtener los
     * elementos ordenados utilice {@link #aListaOrdenada()}.</p>
     *
     * @return iterador sobre las posiciones ocupadas del montículo
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            /** Posición del arreglo que se devolverá a continuación. */
            private int indice;

            /**
             * Indica si quedan posiciones ocupadas por recorrer.
             *
             * @return {@code true} si aún no se alcanzó el tamaño de la cola
             */
            @Override
            public boolean hasNext() {
                return indice < tamano;
            }

            /**
             * Devuelve el siguiente elemento en el orden interno del arreglo.
             *
             * @return elemento de la posición actual
             * @throws NoSuchElementException si ya se recorrieron todos los
             *         elementos
             */
            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elementoEn(indice++);
            }
        };
    }

    /**
     * Reconstruye la propiedad del min-heap hundiendo cada nodo interno desde
     * el último hasta la raíz.
     */
    private void monticulizar() {
        for (int i = tamano / 2 - 1; i >= 0; i--) {
            cribarAbajo(i);
        }
    }

    /**
     * Desplaza un elemento hacia la raíz mientras tenga mayor prioridad que su
     * padre. Se utiliza después de una inserción.
     *
     * @param indice posición inicial del elemento que se debe desplazar
     */
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

    /**
     * Desplaza un elemento hacia las hojas mientras alguno de sus hijos tenga
     * mayor prioridad. Se utiliza después de retirar la raíz o al construir el
     * montículo.
     *
     * @param indice posición inicial del elemento que se debe desplazar
     */
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

    /**
     * Compara los elementos ubicados en dos posiciones del arreglo.
     *
     * @param i primera posición
     * @param j segunda posición
     * @return resultado del comparador entre los elementos de ambas posiciones
     */
    private int compare(int i, int j) {
        return comparador.compare(elementoEn(i), elementoEn(j));
    }

    /**
     * Intercambia los valores de dos posiciones del arreglo interno.
     *
     * @param i primera posición
     * @param j segunda posición
     */
    private void intercambiar(int i, int j) {
        Object temporal = monticulo[i];
        monticulo[i] = monticulo[j];
        monticulo[j] = temporal;
    }

    /**
     * Obtiene y convierte al tipo genérico el elemento de una posición ocupada.
     *
     * @param indice posición del elemento
     * @return elemento almacenado en la posición indicada
     */
    @SuppressWarnings("unchecked")
    private T elementoEn(int indice) {
        return (T) monticulo[indice];
    }
}
