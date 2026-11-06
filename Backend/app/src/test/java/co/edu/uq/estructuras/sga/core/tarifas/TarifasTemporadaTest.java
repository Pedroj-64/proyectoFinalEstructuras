package co.edu.uq.estructuras.sga.core.tarifas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de la tarifa que corresponde a cada temporada y de la validación de las tarifas.
class TarifasTemporadaTest {

    @Test
    @DisplayName("cada temporada devuelve su propia tarifa")
    void tarifaParaCadaTemporada() {
        TarifasTemporada tarifas = new TarifasTemporada(Dinero.de(80_000), Dinero.de(100_000), Dinero.de(150_000));

        assertEquals(Dinero.de(80_000), tarifas.tarifaPara(Temporada.BAJA));
        assertEquals(Dinero.de(100_000), tarifas.tarifaPara(Temporada.MEDIA));
        assertEquals(Dinero.de(150_000), tarifas.tarifaPara(Temporada.ALTA));
    }

    @Test
    @DisplayName("ninguna tarifa puede faltar ni ser negativa, pero sí puede ser cero")
    void tarifasNulasONegativas() {
        Dinero valida = Dinero.de(100_000);
        Dinero negativa = Dinero.de(-1);

        assertThrows(ReglaNegocioExcepcion.class, () -> new TarifasTemporada(null, valida, valida));
        assertThrows(ReglaNegocioExcepcion.class, () -> new TarifasTemporada(valida, null, valida));
        assertThrows(ReglaNegocioExcepcion.class, () -> new TarifasTemporada(valida, valida, null));
        assertThrows(ReglaNegocioExcepcion.class, () -> new TarifasTemporada(negativa, valida, valida));
        assertThrows(ReglaNegocioExcepcion.class, () -> new TarifasTemporada(valida, negativa, valida));
        assertThrows(ReglaNegocioExcepcion.class, () -> new TarifasTemporada(valida, valida, negativa));
        assertDoesNotThrow(() -> new TarifasTemporada(Dinero.CERO, valida, valida));
    }
}
