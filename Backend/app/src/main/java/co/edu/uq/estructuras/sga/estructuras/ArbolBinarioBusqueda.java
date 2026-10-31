package co.edu.uq.estructuras.sga.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.ToIntFunction;

import co.edu.uq.estructuras.sga.excepciones.EstructuraVaciaExcepcion;

public class ArbolBinarioBusqueda<T extends Comparable<T>> implements Iterable<T> {

    private Nodo<T> raiz;
    private int tamano;

    private static final class Nodo<T> {
        private T valor;
        private Nodo<T> izquierda;
        private Nodo<T> derecha;

        // Se supone que en arboles son hojas pero pues recordar
        // las hojas solo cuentan si no tienen hijos, entonces si tiene hijos no es hoja
        private Nodo(T valor) {
            this.valor = valor;
        }
    }

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

    public T buscar(T sonda) {
        return buscarPor(sonda::compareTo);
    }

    /// Busca un elemento comparando con una llave en lugar de un objeto de prueba
    /// completo.
    ///
    /// `comparacionLlave` recibe un valor guardado y debe devolver un número
    /// negativo si la llave
    /// buscada es menor, cero si coincide y un número positivo si es mayor.
    /// Ejemplo: `arbol.buscarPor(apartamento
    /// -> codigo.compareTo(apartamento.obtenerCodigo()))`.
    public T buscarPor(ToIntFunction<T> comparacionLlave) {
        return buscarPor(raiz, comparacionLlave);
    }

    /// Búsqueda recursiva por llave: `comparacionLlave` dice si la llave buscada es
    /// menor (<0), igual (0) o mayor (>0) que el nodo.
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

    public boolean contiene(T sonda) {
        return buscar(sonda) != null;
    }

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
        // Dos hijos: se reemplaza por el sucesor en inorden y se elimina este del
        // subárbol derecho.
        Nodo<T> sucesor = nodoMinimo(nodo.derecha);
        nodo.valor = sucesor.valor;
        nodo.derecha = eliminar(nodo.derecha, sucesor.valor);
        return nodo;
    }

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

    public int altura() {
        return altura(raiz);
    }

    private int altura(Nodo<T> nodo) {
        if (nodo == null) {
            return 0;
        }
        return 1 + Math.max(altura(nodo.izquierda), altura(nodo.derecha));
    }

    public T minimo() {
        if (raiz == null) {
            throw new EstructuraVaciaExcepcion("ArbolBinarioBusqueda");
        }
        return nodoMinimo(raiz).valor;
    }

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

    public int tamano() {
        return tamano;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public void vaciar() {
        raiz = null;
        tamano = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            /// Nodos pendientes de visitar; la cima es el menor.
            private final PilaEnlazada<Nodo<T>> pendiente = new PilaEnlazada<>();

            {
                apilarRamaIzquierda(raiz);
            }

            /// Apila el nodo y toda su rama izquierda; el siguiente en salir es el
            /// menor pendiente.
            private void apilarRamaIzquierda(Nodo<T> nodo) {
                for (Nodo<T> actual = nodo; actual != null; actual = actual.izquierda) {
                    pendiente.apilar(actual);
                }
            }

            /// Quedan nodos mientras la pila de pendientes no esté vacía.
            @Override
            public boolean hasNext() {
                return !pendiente.estaVacia();
            }

            /// Saca el menor pendiente y apila la rama izquierda de su hijo derecho.
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
