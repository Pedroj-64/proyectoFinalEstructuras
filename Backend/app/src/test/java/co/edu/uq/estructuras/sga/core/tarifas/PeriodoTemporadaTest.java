package co.edu.uq.estructuras.sga.core.tarifas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.estructuras.sga.excepciones.RangoFechasInvalidoExcepcion;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de las validaciones de un periodo de temporada y de cómo compara fechas y periodos.
class PeriodoTemporadaTest {

    private static LocalDate diciembre(int dia) {
        return LocalDate.of(2026, 12, dia);
    }

    @Test
    @DisplayName("la temporada y las dos fechas son obligatorias")
    void datosObligatorios() {
        assertThrows(RangoFechasInvalidoExcepcion.class,
                () -> new PeriodoTemporada(null, diciembre(15), diciembre(31)));
        assertThrows(RangoFechasInvalidoExcepcion.class,
                () -> new PeriodoTemporada(Temporada.ALTA, null, diciembre(31)));
        assertThrows(RangoFechasInvalidoExcepcion.class,
                () -> new PeriodoTemporada(Temporada.ALTA, diciembre(15), null));
    }

    @Test
    @DisplayName("el fin no puede ser antes del inicio, pero sí el mismo día")
    void finAntesDelInicio() {
        assertThrows(RangoFechasInvalidoExcepcion.class,
                () -> new PeriodoTemporada(Temporada.ALTA, diciembre(31), diciembre(15)));
        assertDoesNotThrow(() -> new PeriodoTemporada(Temporada.ALTA, diciembre(15), diciembre(15)));
    }

    @Test
    @DisplayName("contiene incluye el primer y el último día del periodo")
    void contieneIncluyeAmbosExtremos() {
        PeriodoTemporada navidad = new PeriodoTemporada(Temporada.ALTA, diciembre(15), diciembre(31));

        assertTrue(navidad.contiene(diciembre(15)));
        assertTrue(navidad.contiene(diciembre(31)));
        assertFalse(navidad.contiene(diciembre(14)));
        assertFalse(navidad.contiene(LocalDate.of(2027, 1, 1)));
    }

    @Test
    @DisplayName("dos periodos se solapan si comparten al menos un día")
    void solapanAlCompartirUnDia() {
        PeriodoTemporada primero = new PeriodoTemporada(Temporada.MEDIA, diciembre(1), diciembre(15));
        PeriodoTemporada segundo = new PeriodoTemporada(Temporada.ALTA, diciembre(15), diciembre(31));

        assertTrue(primero.solapa(segundo));
        assertTrue(segundo.solapa(primero));
    }

    @Test
    @DisplayName("dos periodos seguidos sin días en común no se solapan")
    void noSolapanSinDiasEnComun() {
        PeriodoTemporada primero = new PeriodoTemporada(Temporada.MEDIA, diciembre(1), diciembre(14));
        PeriodoTemporada segundo = new PeriodoTemporada(Temporada.ALTA, diciembre(15), diciembre(31));

        assertFalse(primero.solapa(segundo));
        assertFalse(segundo.solapa(primero));
    }
}
