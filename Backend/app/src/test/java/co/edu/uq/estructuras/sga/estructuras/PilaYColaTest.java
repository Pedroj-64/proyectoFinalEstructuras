package co.edu.uq.estructuras.sga.estructuras;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uq.estructuras.sga.excepciones.EstructuraVaciaExcepcion;

class PilaYColaTest {

    @Test
    @DisplayName("la pila saca en orden LIFO")
    void pilaEsLifo() {
        PilaEnlazada<String> pila = new PilaEnlazada<>();
        pila.apilar("a");
        pila.apilar("b");
        assertEquals("b", pila.ver());
        assertEquals("b", pila.desapilar());
        assertEquals("a", pila.desapilar());
        assertTrue(pila.estaVacia());
    }

    @Test
    @DisplayName("desapilar una pila vacía lanza EstructuraVaciaExcepcion")
    void pilaVaciaLanza() {
        assertThrows(EstructuraVaciaExcepcion.class, () -> new PilaEnlazada<String>().desapilar());
    }

    @Test
    @DisplayName("la cola saca en orden FIFO")
    void colaEsFifo() {
        ColaEnlazada<Integer> cola = new ColaEnlazada<>();
        cola.encolar(1);
        cola.encolar(2);
        cola.encolar(3);
        assertEquals(1, cola.ver());
        assertEquals(1, cola.desencolar());
        assertEquals(2, cola.desencolar());
        assertEquals(1, cola.tamano());
    }

    @Test
    @DisplayName("desencolar una cola vacía lanza EstructuraVaciaExcepcion")
    void colaVaciaLanza() {
        assertThrows(EstructuraVaciaExcepcion.class, () -> new ColaEnlazada<String>().desencolar());
    }
}
