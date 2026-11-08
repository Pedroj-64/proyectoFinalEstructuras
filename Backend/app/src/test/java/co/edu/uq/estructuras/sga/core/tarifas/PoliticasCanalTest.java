package co.edu.uq.estructuras.sga.core.tarifas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.estructuras.sga.core.reservas.CanalReserva;
import co.edu.uq.estructuras.sga.core.reservas.EstadoReserva;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de la fábrica de políticas por canal y de las reglas de cada canal.
/// Las variables se declaran con el tipo de la interfaz [PoliticaCanal] (patrón Strategy).
class PoliticasCanalTest {

    @Test
    @DisplayName("cada canal recibe una política de ese mismo canal")
    void cadaCanalRecibeSuPolitica() {
        for (CanalReserva canal : CanalReserva.values()) {
            PoliticaCanal politica = PoliticasCanal.paraCanal(canal);
            assertEquals(canal, politica.canal(), "política entregada para " + canal);
        }
    }

    @Test
    @DisplayName("una reserva del portal nace pendiente y vence sin anticipo")
    void portalNacePendienteYVence() {
        PoliticaCanal portal = PoliticasCanal.paraCanal(CanalReserva.PORTAL);

        assertEquals(EstadoReserva.PENDIENTE, portal.estadoInicial());
        assertTrue(portal.venceSinAnticipo());
    }

    @Test
    @DisplayName("las reservas directas y externas nacen confirmadas y no vencen")
    void directoYExternoNacenConfirmadas() {
        PoliticaCanal directo = PoliticasCanal.paraCanal(CanalReserva.DIRECTO);
        PoliticaCanal externo = PoliticasCanal.paraCanal(CanalReserva.EXTERNO);

        assertEquals(EstadoReserva.CONFIRMADA, directo.estadoInicial());
        assertFalse(directo.venceSinAnticipo());
        assertEquals(EstadoReserva.CONFIRMADA, externo.estadoInicial());
        assertFalse(externo.venceSinAnticipo());
    }

    @Test
    @DisplayName("pedir dos veces la política de un canal devuelve la misma instancia")
    void mismaInstanciaParaElMismoCanal() {
        for (CanalReserva canal : CanalReserva.values()) {
            assertSame(PoliticasCanal.paraCanal(canal), PoliticasCanal.paraCanal(canal), "instancia para " + canal);
        }
    }
}
