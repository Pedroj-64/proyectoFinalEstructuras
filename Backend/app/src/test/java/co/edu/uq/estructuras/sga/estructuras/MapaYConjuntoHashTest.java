package co.edu.uq.estructuras.sga.estructuras;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MapaYConjuntoHashTest {

    @Test
    @DisplayName("el mapa guarda, reemplaza y quita llaves")
    void mapaBasico() {
        MapaHash<String, Integer> mapa = new MapaHash<>();
        mapa.poner("a", 1);
        mapa.poner("a", 2);
        assertEquals(2, mapa.obtener("a"));
        assertEquals(1, mapa.tamano());
        assertEquals(2, mapa.quitar("a"));
        assertNull(mapa.obtener("a"));
        assertFalse(mapa.contieneLlave("a"));
    }

    @Test
    @DisplayName("el mapa crece sin perder datos")
    void mapaCrece() {
        MapaHash<Integer, Integer> mapa = new MapaHash<>();
        for (int i = 0; i < 1000; i++) {
            mapa.poner(i, i * 2);
        }
        assertEquals(1000, mapa.tamano());
        for (int i = 0; i < 1000; i++) {
            assertEquals(i * 2, mapa.obtener(i));
        }
    }

    @Test
    @DisplayName("calcularSiAusente solo crea el valor una vez")
    void calcularSiAusente() {
        MapaHash<String, ListaDoblementeEnlazada<Integer>> mapa = new MapaHash<>();
        mapa.calcularSiAusente("x", ListaDoblementeEnlazada::new).agregarUltimo(1);
        mapa.calcularSiAusente("x", ListaDoblementeEnlazada::new).agregarUltimo(2);
        assertEquals(2, mapa.obtener("x").tamano());
    }

    @Test
    @DisplayName("el conjunto no admite repetidos")
    void conjunto() {
        ConjuntoHash<String> conjunto = new ConjuntoHash<>();
        assertTrue(conjunto.agregar("a"));
        assertFalse(conjunto.agregar("a"));
        assertTrue(conjunto.contiene("a"));
        assertTrue(conjunto.quitar("a"));
        assertTrue(conjunto.estaVacia());
    }
}
