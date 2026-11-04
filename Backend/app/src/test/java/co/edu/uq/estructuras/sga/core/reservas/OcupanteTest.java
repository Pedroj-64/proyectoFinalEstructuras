package co.edu.uq.estructuras.sga.core.reservas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de los datos obligatorios de un ocupante, su edad y si paga la noche.
class OcupanteTest {

    private static final LocalDate NACIMIENTO = LocalDate.of(2000, 5, 20);

    @Test
    @DisplayName("el nombre y la fecha de nacimiento son obligatorios")
    void datosObligatorios() {
        assertThrows(ReglaNegocioExcepcion.class, () -> new Ocupante(null, NACIMIENTO));
        assertThrows(ReglaNegocioExcepcion.class, () -> new Ocupante("", NACIMIENTO));
        assertThrows(ReglaNegocioExcepcion.class, () -> new Ocupante("   ", NACIMIENTO));
        assertThrows(ReglaNegocioExcepcion.class, () -> new Ocupante("Ana Gómez", null));
    }

    @Test
    @DisplayName("la edad sube el día del cumpleaños, no antes")
    void edadAntesYDespuesDelCumpleanos() {
        Ocupante ana = new Ocupante("Ana Gómez", NACIMIENTO);
        assertEquals(25, ana.edadEn(LocalDate.of(2026, 5, 19)));
        assertEquals(26, ana.edadEn(LocalDate.of(2026, 5, 20)));
        assertEquals(26, ana.edadEn(LocalDate.of(2026, 12, 1)));
    }

    @Test
    @DisplayName("un niño menor de la edad límite no paga y un adulto sí")
    void pagaSegunLaEdad() {
        LocalDate noche = LocalDate.of(2026, 3, 10);
        Ocupante nino = new Ocupante("Luis Gómez", noche.minusYears(Ocupante.EDAD_LIMITE_GRATIS - 1));
        Ocupante adulto = new Ocupante("Ana Gómez", NACIMIENTO);
        Ocupante cumpleLimite = new Ocupante("Sara Gómez", noche.minusYears(Ocupante.EDAD_LIMITE_GRATIS));

        assertFalse(nino.pagaEn(noche));
        assertTrue(adulto.pagaEn(noche));
        assertTrue(cumpleLimite.pagaEn(noche), "el día que cumple la edad límite ya paga");
    }
}
