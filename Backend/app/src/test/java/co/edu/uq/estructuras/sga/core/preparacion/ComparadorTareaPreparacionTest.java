package co.edu.uq.estructuras.sga.core.preparacion;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ComparadorTareaPreparacionTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 8, 0);

    private TareaPreparacion tarea(String cod, boolean hoy, LocalTime llegada, int minutosEncolado) {
        return new TareaPreparacion(cod, hoy, llegada, T0.plusMinutes(minutosEncolado));
    }

    private int comparar(TareaPreparacion a, TareaPreparacion b) {
        return ComparadorTareaPreparacion.INSTANCIA.compare(a, b);
    }

    @Test
    @DisplayName("la llegada de hoy va antes que la que no es de hoy")
    void hoyPrimero() {
        assertTrue(comparar(tarea("A", true, null, 10), tarea("B", false, LocalTime.of(9, 0), 0)) < 0);
    }

    @Test
    @DisplayName("entre llegadas de hoy gana la hora más temprana y sin hora va al final")
    void horaDeLlegada() {
        assertTrue(comparar(tarea("A", true, LocalTime.of(10, 0), 5), tarea("B", true, LocalTime.of(14, 0), 0)) < 0);
        assertTrue(comparar(tarea("A", true, LocalTime.of(10, 0), 5), tarea("B", true, null, 0)) < 0);
    }

    @Test
    @DisplayName("a igual prioridad y hora gana la que entró primero")
    void fifo() {
        assertTrue(comparar(tarea("A", false, null, 1), tarea("B", false, null, 2)) < 0);
        assertEquals(0, comparar(tarea("A", false, null, 1), tarea("A", false, null, 1)));
    }
}
