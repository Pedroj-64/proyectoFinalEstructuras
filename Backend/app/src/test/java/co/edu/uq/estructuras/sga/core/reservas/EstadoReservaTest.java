package co.edu.uq.estructuras.sga.core.reservas;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas del ciclo de vida de una reserva y de qué estados se consideran activos.
class EstadoReservaTest {

    @Test
    @DisplayName("el ciclo normal es Pendiente → Confirmada → En curso → Finalizada")
    void cicloNormal() {
        assertTrue(EstadoReserva.PENDIENTE.puedePasarA(EstadoReserva.CONFIRMADA));
        assertTrue(EstadoReserva.CONFIRMADA.puedePasarA(EstadoReserva.EN_CURSO));
        assertTrue(EstadoReserva.EN_CURSO.puedePasarA(EstadoReserva.FINALIZADA));
    }

    @Test
    @DisplayName("finalizada, cancelada y no presentado no pueden pasar a ningún estado")
    void estadosFinalesNoTienenSalida() {
        EstadoReserva[] finales = {
            EstadoReserva.FINALIZADA, EstadoReserva.CANCELADA, EstadoReserva.NO_PRESENTADO
        };
        for (EstadoReserva origen : finales) {
            for (EstadoReserva destino : EstadoReserva.values()) {
                assertFalse(origen.puedePasarA(destino), origen + " no debería poder pasar a " + destino);
            }
        }
    }

    @Test
    @DisplayName("no presentado solo se alcanza desde confirmada")
    void noPresentadoSoloDesdeConfirmada() {
        assertTrue(EstadoReserva.CONFIRMADA.puedePasarA(EstadoReserva.NO_PRESENTADO));
        for (EstadoReserva origen : EstadoReserva.values()) {
            if (origen != EstadoReserva.CONFIRMADA) {
                assertFalse(origen.puedePasarA(EstadoReserva.NO_PRESENTADO),
                        origen + " no debería poder pasar a NO_PRESENTADO");
            }
        }
    }

    @Test
    @DisplayName("solo pendiente, confirmada y en curso son reservas activas")
    void estadosActivos() {
        assertTrue(EstadoReserva.PENDIENTE.estaActiva());
        assertTrue(EstadoReserva.CONFIRMADA.estaActiva());
        assertTrue(EstadoReserva.EN_CURSO.estaActiva());
        assertFalse(EstadoReserva.FINALIZADA.estaActiva());
        assertFalse(EstadoReserva.CANCELADA.estaActiva());
        assertFalse(EstadoReserva.NO_PRESENTADO.estaActiva());
    }
}
