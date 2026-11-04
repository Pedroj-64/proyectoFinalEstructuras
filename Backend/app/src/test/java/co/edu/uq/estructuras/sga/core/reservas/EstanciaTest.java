package co.edu.uq.estructuras.sga.core.reservas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.estructuras.sga.excepciones.RangoFechasInvalidoExcepcion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas del rango de fechas de una estancia: noches, validaciones, solapamiento y recorrido.
class EstanciaTest {

    private static LocalDate marzo(int dia) {
        return LocalDate.of(2026, 3, dia);
    }

    @Test
    @DisplayName("del 10 al 13 de marzo son 3 noches")
    void cuentaNoches() {
        assertEquals(3, new Estancia(marzo(10), marzo(13)).noches());
        assertEquals(1, new Estancia(marzo(10), marzo(11)).noches());
    }

    @Test
    @DisplayName("la salida debe ser posterior a la entrada")
    void salidaAntesOIgualALaEntrada() {
        assertThrows(RangoFechasInvalidoExcepcion.class, () -> new Estancia(marzo(13), marzo(10)));
        assertThrows(RangoFechasInvalidoExcepcion.class, () -> new Estancia(marzo(10), marzo(10)));
    }

    @Test
    @DisplayName("las fechas de entrada y salida son obligatorias")
    void fechasNulas() {
        assertThrows(RangoFechasInvalidoExcepcion.class, () -> new Estancia(null, marzo(13)));
        assertThrows(RangoFechasInvalidoExcepcion.class, () -> new Estancia(marzo(10), null));
    }

    @Test
    @DisplayName("dos estancias se solapan si comparten al menos una noche")
    void solapanAlCompartirNoche() {
        Estancia primera = new Estancia(marzo(10), marzo(13));
        Estancia segunda = new Estancia(marzo(12), marzo(15));
        assertTrue(primera.solapa(segunda));
        assertTrue(segunda.solapa(primera));
    }

    @Test
    @DisplayName("no se solapan si una sale el mismo día que la otra entra")
    void noSolapanSiSaleCuandoLaOtraEntra() {
        Estancia primera = new Estancia(marzo(10), marzo(13));
        Estancia segunda = new Estancia(marzo(13), marzo(15));
        assertFalse(primera.solapa(segunda));
        assertFalse(segunda.solapa(primera));
    }

    @Test
    @DisplayName("la noche de entrada pertenece a la estancia y el día de salida no")
    void incluyeNocheDeEntradaPeroNoLaSalida() {
        Estancia estancia = new Estancia(marzo(10), marzo(13));
        assertTrue(estancia.incluyeNoche(marzo(10)));
        assertFalse(estancia.incluyeNoche(marzo(13)));
    }

    @Test
    @DisplayName("recorrerla con for-each entrega exactamente sus noches, en orden")
    void recorridoConForEach() {
        List<LocalDate> noches = new ArrayList<>();
        for (LocalDate noche : new Estancia(marzo(10), marzo(13))) {
            noches.add(noche);
        }
        assertEquals(List.of(marzo(10), marzo(11), marzo(12)), noches);
    }
}
