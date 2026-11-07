package co.edu.uq.estructuras.sga.estructuras;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArbolBinarioBusquedaTest {

    private ArbolBinarioBusqueda<Integer> arbol(int... valores) {
        ArbolBinarioBusqueda<Integer> arbol = new ArbolBinarioBusqueda<>();
        for (int v : valores) {
            arbol.insertar(v);
        }
        return arbol;
    }

    @Test
    @DisplayName("el recorrido en orden sale ascendente y no admite duplicados")
    void enOrden() {
        ArbolBinarioBusqueda<Integer> arbol = arbol(5, 3, 8, 1, 4, 9);
        assertFalse(arbol.insertar(5));
        assertArrayEquals(new Integer[] {1, 3, 4, 5, 8, 9}, arbol.enOrden().aArreglo(Integer[]::new));
        assertEquals(1, arbol.minimo());
        assertEquals(9, arbol.maximo());
    }

    @Test
    @DisplayName("elimina hojas, nodos con un hijo y nodos con dos hijos")
    void elimina() {
        ArbolBinarioBusqueda<Integer> arbol = arbol(5, 3, 8, 1, 4, 7, 9);
        assertTrue(arbol.eliminar(1));
        assertTrue(arbol.eliminar(3));
        assertTrue(arbol.eliminar(5));
        assertFalse(arbol.eliminar(99));
        assertArrayEquals(new Integer[] {4, 7, 8, 9}, arbol.enOrden().aArreglo(Integer[]::new));
    }

    @Test
    @DisplayName("busca por valor y calcula la altura")
    void buscaYAltura() {
        ArbolBinarioBusqueda<Integer> arbol = arbol(2, 1, 3);
        assertEquals(3, arbol.buscar(3));
        assertNull(arbol.buscar(7));
        assertEquals(2, arbol.altura());
    }
}
