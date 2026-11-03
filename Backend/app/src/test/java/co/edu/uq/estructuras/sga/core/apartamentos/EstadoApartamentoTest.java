package co.edu.uq.estructuras.sga.core.apartamentos;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de las transiciones permitidas entre estados de un apartamento.
class EstadoApartamentoTest {

    @Test
    @DisplayName("el ciclo normal es Preparado → Ocupado → Pendiente → En preparación → Preparado")
    void cicloNormal() {
        assertTrue(EstadoApartamento.PREPARADO.puedePasarA(EstadoApartamento.OCUPADO));
        assertTrue(EstadoApartamento.OCUPADO.puedePasarA(EstadoApartamento.PENDIENTE_PREPARACION));
        assertTrue(EstadoApartamento.PENDIENTE_PREPARACION.puedePasarA(EstadoApartamento.EN_PREPARACION));
        assertTrue(EstadoApartamento.EN_PREPARACION.puedePasarA(EstadoApartamento.PREPARADO));
    }

    @Test
    @DisplayName("un apartamento ocupado no puede quedar preparado ni fuera de servicio directamente")
    void ocupadoSoloPasaAPendiente() {
        assertFalse(EstadoApartamento.OCUPADO.puedePasarA(EstadoApartamento.PREPARADO));
        assertFalse(EstadoApartamento.OCUPADO.puedePasarA(EstadoApartamento.FUERA_DE_SERVICIO));
    }

    @Test
    @DisplayName("fuera de servicio vuelve a servicio por preparación o directamente a preparado")
    void salidaDeFueraDeServicio() {
        assertTrue(EstadoApartamento.FUERA_DE_SERVICIO.puedePasarA(EstadoApartamento.PENDIENTE_PREPARACION));
        assertTrue(EstadoApartamento.FUERA_DE_SERVICIO.puedePasarA(EstadoApartamento.PREPARADO));
        assertFalse(EstadoApartamento.FUERA_DE_SERVICIO.puedePasarA(EstadoApartamento.OCUPADO));
    }

    @Test
    @DisplayName("cada estado expone su nombre en español")
    void nombresMostrados() {
        assertTrue(EstadoApartamento.PENDIENTE_PREPARACION.nombreMostrado().startsWith("Pendiente"));
        assertTrue(EstadoApartamento.FUERA_DE_SERVICIO.nombreMostrado().equals("Fuera de servicio"));
    }
}
