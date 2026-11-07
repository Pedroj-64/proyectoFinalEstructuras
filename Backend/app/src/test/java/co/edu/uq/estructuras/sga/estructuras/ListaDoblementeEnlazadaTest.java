package co.edu.uq.estructuras.sga.estructuras;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ListaDoblementeEnlazadaTest {

    @Test
    @DisplayName("agrega por ambos extremos y respeta el orden")
    void agregaPorAmbosExtremos() {
        ListaDoblementeEnlazada<Integer> lista = ListaDoblementeEnlazada.de(2, 3);
        lista.agregarPrimero(1);
        lista.agregarUltimo(4);
        assertArrayEquals(new Integer[] {1, 2, 3, 4}, lista.aArreglo(Integer[]::new));
        assertEquals(4, lista.tamano());
    }

    @Test
    @DisplayName("quita primero, último y por valor")
    void quita() {
        ListaDoblementeEnlazada<Integer> lista = ListaDoblementeEnlazada.de(1, 2, 3, 4);
        assertEquals(1, lista.quitarPrimero());
        assertEquals(4, lista.quitarUltimo());
        assertTrue(lista.quitar(2));
        assertFalse(lista.quitar(99));
        assertArrayEquals(new Integer[] {3}, lista.aArreglo(Integer[]::new));
    }

    @Test
    @DisplayName("filtra, ordena y no modifica la original")
    void filtraYOrdena() {
        ListaDoblementeEnlazada<Integer> lista = ListaDoblementeEnlazada.de(5, 2, 8, 1);
        assertArrayEquals(new Integer[] {2, 8}, lista.filtrar(n -> n % 2 == 0).aArreglo(Integer[]::new));
        assertArrayEquals(new Integer[] {1, 2, 5, 8}, lista.ordenada(Integer::compare).aArreglo(Integer[]::new));
        assertEquals(5, lista.obtener(0));
    }

    @Test
    @DisplayName("la reversa recorre al revés")
    void reversa() {
        StringBuilder sb = new StringBuilder();
        for (int n : ListaDoblementeEnlazada.de(1, 2, 3).reversa()) {
            sb.append(n);
        }
        assertEquals("321", sb.toString());
    }
}
