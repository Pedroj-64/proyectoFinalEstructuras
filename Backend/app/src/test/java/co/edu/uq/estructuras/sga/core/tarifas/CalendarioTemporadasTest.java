package co.edu.uq.estructuras.sga.core.tarifas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.RangoFechasInvalidoExcepcion;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de cómo el calendario asigna una temporada a cada fecha y evita periodos cruzados.
class CalendarioTemporadasTest {

    private static final PeriodoTemporada SEMANA_SANTA =
            new PeriodoTemporada(Temporada.ALTA, LocalDate.of(2026, 3, 29), LocalDate.of(2026, 4, 5));
    private static final PeriodoTemporada MITAD_DE_ANO =
            new PeriodoTemporada(Temporada.MEDIA, LocalDate.of(2026, 6, 15), LocalDate.of(2026, 7, 15));
    private static final PeriodoTemporada NAVIDAD =
            new PeriodoTemporada(Temporada.ALTA, LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 15));

    @Test
    @DisplayName("una fecha fuera de todo periodo es temporada baja")
    void fueraDeTodoPeriodoEsBaja() {
        CalendarioTemporadas calendario = new CalendarioTemporadas();
        assertEquals(Temporada.BAJA, calendario.temporadaDe(LocalDate.of(2026, 3, 10)), "calendario vacío");

        calendario.agregarPeriodo(NAVIDAD);
        assertEquals(Temporada.BAJA, calendario.temporadaDe(LocalDate.of(2026, 3, 10)));
    }

    @Test
    @DisplayName("una fecha dentro de un periodo de temporada alta es alta")
    void dentroDePeriodoAltoEsAlta() {
        CalendarioTemporadas calendario = new CalendarioTemporadas();
        calendario.agregarPeriodo(NAVIDAD);

        assertEquals(Temporada.ALTA, calendario.temporadaDe(LocalDate.of(2026, 12, 24)));
    }

    @Test
    @DisplayName("no se puede agregar un periodo que se cruza con otro")
    void periodoCruzadoLanzaExcepcion() {
        CalendarioTemporadas calendario = new CalendarioTemporadas();
        calendario.agregarPeriodo(NAVIDAD);
        PeriodoTemporada cruzado =
                new PeriodoTemporada(Temporada.MEDIA, LocalDate.of(2027, 1, 10), LocalDate.of(2027, 1, 31));

        assertThrows(RangoFechasInvalidoExcepcion.class, () -> calendario.agregarPeriodo(cruzado));
        assertEquals(1, calendario.periodos().tamano());
    }

    @Test
    @DisplayName("el periodo es obligatorio, esté vacío o no el calendario")
    void periodoNuloLanzaExcepcion() {
        CalendarioTemporadas calendario = new CalendarioTemporadas();
        RangoFechasInvalidoExcepcion error =
                assertThrows(RangoFechasInvalidoExcepcion.class, () -> calendario.agregarPeriodo(null));
        assertEquals("El periodo es obligatorio", error.getMessage());
        assertEquals(0, calendario.periodos().tamano());

        calendario.agregarPeriodo(NAVIDAD);
        assertThrows(RangoFechasInvalidoExcepcion.class, () -> calendario.agregarPeriodo(null));
        assertEquals(1, calendario.periodos().tamano());
    }

    @Test
    @DisplayName("si un periodo nuevo es inválido, reemplazarPeriodos conserva el calendario anterior")
    void reemplazoInvalidoConservaElCalendario() {
        CalendarioTemporadas calendario = new CalendarioTemporadas();
        calendario.agregarPeriodo(NAVIDAD);
        PeriodoTemporada cruzaSemanaSanta =
                new PeriodoTemporada(Temporada.MEDIA, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        assertThrows(RangoFechasInvalidoExcepcion.class,
                () -> calendario.reemplazarPeriodos(ListaDoblementeEnlazada.de(SEMANA_SANTA, cruzaSemanaSanta)));

        ListaDoblementeEnlazada<PeriodoTemporada> periodos = calendario.periodos();
        assertEquals(1, periodos.tamano());
        assertEquals(NAVIDAD, periodos.obtener(0));
        assertEquals(Temporada.BAJA, calendario.temporadaDe(LocalDate.of(2026, 4, 2)));
    }

    @Test
    @DisplayName("periodos() sale ordenado por fecha de inicio aunque se agreguen en otro orden")
    void periodosOrdenadosPorInicio() {
        CalendarioTemporadas calendario = new CalendarioTemporadas();
        calendario.agregarPeriodo(NAVIDAD);
        calendario.agregarPeriodo(SEMANA_SANTA);
        calendario.agregarPeriodo(MITAD_DE_ANO);

        ListaDoblementeEnlazada<PeriodoTemporada> periodos = calendario.periodos();
        assertEquals(3, periodos.tamano());
        assertEquals(SEMANA_SANTA, periodos.obtener(0));
        assertEquals(MITAD_DE_ANO, periodos.obtener(1));
        assertEquals(NAVIDAD, periodos.obtener(2));
    }
}
