package co.edu.uq.estructuras.sga.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas del reloj simulado: hora base, viaje en el tiempo y reinicio.
class ServicioRelojTest {

    /// Reloj fijo el 9 de noviembre de 2026 a las 12:00 UTC.
    private static final Clock FIJO = Clock.fixed(Instant.parse("2026-11-09T12:00:00Z"), ZoneOffset.UTC);

    @Test
    @DisplayName("sin desfase devuelve la hora base")
    void horaBase() {
        ServicioReloj reloj = new ServicioReloj(FIJO);

        assertEquals(LocalDateTime.of(2026, 11, 9, 12, 0), reloj.ahora());
        assertFalse(reloj.estaSimulado());
    }

    @Test
    @DisplayName("viajarA mueve el reloj a la fecha y hora indicadas")
    void viajaEnElTiempo() {
        ServicioReloj reloj = new ServicioReloj(FIJO);

        reloj.viajarA(LocalDateTime.of(2026, 11, 10, 13, 0));

        assertEquals(LocalDateTime.of(2026, 11, 10, 13, 0), reloj.ahora());
        assertTrue(reloj.estaSimulado());
    }

    @Test
    @DisplayName("también puede viajar hacia atrás")
    void viajaHaciaAtras() {
        ServicioReloj reloj = new ServicioReloj(FIJO);

        reloj.viajarA(LocalDateTime.of(2026, 11, 8, 8, 0));

        assertEquals(LocalDateTime.of(2026, 11, 8, 8, 0), reloj.ahora());
        assertTrue(reloj.estaSimulado());
    }

    @Test
    @DisplayName("reinicio vuelve a la hora base")
    void reinicia() {
        ServicioReloj reloj = new ServicioReloj(FIJO);
        reloj.viajarA(LocalDateTime.of(2026, 11, 10, 13, 0));

        reloj.reinicio();

        assertEquals(LocalDateTime.of(2026, 11, 9, 12, 0), reloj.ahora());
        assertFalse(reloj.estaSimulado());
    }
}
