package co.edu.uq.estructuras.sga.estructuras;

import java.util.Iterator;

public class ArbolBinarioBusqueda<T extends Comparable<T>> implements Iterable<T> {

    private Nodo<T> raiz;
    private int tamano;

    private static final class Nodo<T> {
        private T valor;
        private Nodo<T> izquierda;
        private Nodo<T> derecha;
        //Se supone que en arboles son hojas pero pues recordar
        //las hojas solo cuentan si no tienen hijos, entonces si tiene hijos no es hoja
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

    @Override
    public Iterator<T> iterator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'iterator'");
    }

}
