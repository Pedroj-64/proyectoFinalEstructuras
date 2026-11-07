package co.edu.uq.estructuras.sga.estructuras;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ListaCircularTest {

    @Test
    @DisplayName("avanzar da la vuelta al llegar al final")
    void daLaVuelta() {
        ListaCircular<String> lista = new ListaCircular<>();
        lista.agregar("a");
        lista.agregar("b");
        lista.agregar("c");
        String primero = lista.actual();
        lista.avanzar();
        lista.avanzar();
        lista.avanzar();
        assertEquals(primero, lista.actual());
    }

    @Test
    @DisplayName("obtenerRotado con tantos pasos como elementos vuelve al mismo")
    void rotadoCompleto() {
        ListaCircular<Integer> lista = new ListaCircular<>();
        lista.agregar(1);
        lista.agregar(2);
        lista.agregar(3);
        assertEquals(lista.obtenerRotado(0), lista.obtenerRotado(3));
        assertEquals(lista.obtenerRotado(1), lista.obtenerRotado(4));
    }
}
