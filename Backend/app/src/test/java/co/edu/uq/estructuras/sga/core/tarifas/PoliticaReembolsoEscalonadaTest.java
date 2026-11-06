package co.edu.uq.estructuras.sga.core.tarifas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de los bordes de la política de reembolso escalonada.
class PoliticaReembolsoEscalonadaTest {

    private static final LocalDate INGRESO = LocalDate.of(2026, 12, 20);

    /// Se declara con el tipo de la interfaz (patrón Strategy): las pruebas no dependen de la regla concreta.
    private final PoliticaReembolso politica = new PoliticaReembolsoEscalonada();

    @Test
    @DisplayName("con 7 días o más de anticipación se devuelve el 100 %")
    void sieteDiasOMasDevuelveTodo() {
        assertEquals(100, politica.porcentajeReembolso(INGRESO.minusDays(10), INGRESO), "10 días antes");
        assertEquals(100, politica.porcentajeReembolso(INGRESO.minusDays(7), INGRESO), "exactamente 7 días antes");
    }

    @Test
    @DisplayName("de 2 a 6 días de anticipación se devuelve el 50 %")
    void deDosASeisDiasDevuelveLaMitad() {
        assertEquals(50, politica.porcentajeReembolso(INGRESO.minusDays(6), INGRESO), "6 días antes");
        assertEquals(50, politica.porcentajeReembolso(INGRESO.minusDays(2), INGRESO), "exactamente 2 días antes");
    }

    @Test
    @DisplayName("con menos de 2 días, el mismo día o después de la entrada no se devuelve nada")
    void menosDeDosDiasNoDevuelveNada() {
        assertEquals(0, politica.porcentajeReembolso(INGRESO.minusDays(1), INGRESO), "1 día antes");
        assertEquals(0, politica.porcentajeReembolso(INGRESO, INGRESO), "el mismo día de la entrada");
        assertEquals(0, politica.porcentajeReembolso(INGRESO.plusDays(1), INGRESO), "después de la entrada");
    }
}
