package co.edu.uq.estructuras.sga.core.tarifas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de que la vista previa de un reembolso conserva sus datos y se compara por valor.
class VistaPreviaReembolsoTest {

    @Test
    @DisplayName("la vista previa guarda todos sus datos")
    void guardaSusDatos() {
        VistaPreviaReembolso vista = new VistaPreviaReembolso("R-0001", Dinero.de(300_000), 50,
                Dinero.de(150_000), Dinero.de(150_000), 4);

        assertEquals("R-0001", vista.idReserva());
        assertEquals(Dinero.de(300_000), vista.pagado());
        assertEquals(50, vista.porcentaje());
        assertEquals(Dinero.de(150_000), vista.reembolso());
        assertEquals(Dinero.de(150_000), vista.penalidad());
        assertEquals(4L, vista.diasAntesIngreso());
    }

    @Test
    @DisplayName("dos vistas previas con los mismos datos son iguales")
    void mismosDatosSonIguales() {
        VistaPreviaReembolso una = new VistaPreviaReembolso("R-0001", Dinero.de(300_000), 50,
                Dinero.de(150_000), Dinero.de(150_000), 4);
        VistaPreviaReembolso otra = new VistaPreviaReembolso("R-0001", Dinero.de(300_000), 50,
                Dinero.de(150_000), Dinero.de(150_000), 4);

        assertEquals(una, otra);
        assertEquals(una.hashCode(), otra.hashCode());
        assertNotEquals(una, new VistaPreviaReembolso("R-0001", Dinero.de(300_000), 100,
                Dinero.de(300_000), Dinero.CERO, 10));
    }
}
