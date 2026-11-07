package co.edu.uq.estructuras.sga.estructuras;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ColaPrioridadMonticuloTest {

    @Test
    @DisplayName("saca los elementos del menor al mayor según el comparador")
    void sacaEnOrden() {
        ColaPrioridadMonticulo<Integer> cola = new ColaPrioridadMonticulo<>(Comparator.naturalOrder());
        for (int n : new int[] {5, 1, 4, 2, 3}) {
            cola.ofrecer(n);
        }
        assertEquals(1, cola.ver());
        for (int esperado = 1; esperado <= 5; esperado++) {
            assertEquals(esperado, cola.sacar());
        }
        assertTrue(cola.estaVacia());
    }

    @Test
    @DisplayName("quitarSi elimina los que cumplen y conserva el orden del resto")
    void quitarSi() {
        ColaPrioridadMonticulo<Integer> cola = new ColaPrioridadMonticulo<>(Comparator.naturalOrder());
        for (int n = 1; n <= 6; n++) {
            cola.ofrecer(n);
        }
        assertEquals(3, cola.quitarSi(n -> n % 2 == 0));
        assertArrayEquals(new Integer[] {1, 3, 5}, cola.aListaOrdenada().aArreglo(Integer[]::new));
    }

    @Test
    @DisplayName("aListaOrdenada no saca nada de la cola")
    void listaOrdenadaNoConsume() {
        ColaPrioridadMonticulo<Integer> cola = new ColaPrioridadMonticulo<>(Comparator.reverseOrder());
        cola.ofrecer(1);
        cola.ofrecer(9);
        assertArrayEquals(new Integer[] {9, 1}, cola.aListaOrdenada().aArreglo(Integer[]::new));
        assertEquals(2, cola.tamano());
    }
}
