package co.edu.uq.estructuras.sga.core.tarifas;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de las temporadas tarifarias y de sus nombres para la interfaz.
class TemporadaTest {

    @Test
    @DisplayName("las temporadas son baja, media y alta, en ese orden")
    void valoresEnOrden() {
        assertArrayEquals(new Temporada[] {Temporada.BAJA, Temporada.MEDIA, Temporada.ALTA}, Temporada.values());
    }

    @Test
    @DisplayName("cada temporada expone su nombre en español")
    void nombresMostrados() {
        assertEquals("Baja", Temporada.BAJA.nombreMostrado());
        assertEquals("Media", Temporada.MEDIA.nombreMostrado());
        assertEquals("Alta", Temporada.ALTA.nombreMostrado());
    }
}
