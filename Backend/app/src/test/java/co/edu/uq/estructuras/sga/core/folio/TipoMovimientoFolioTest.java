package co.edu.uq.estructuras.sga.core.folio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas del signo de cada tipo de movimiento del folio y de cuáles se pueden revertir.
class TipoMovimientoFolioTest {

    @Test
    @DisplayName("cargo, anulación de pago y devolución suman a la deuda; pago y anulación de cargo restan")
    void signoDeCadaTipo() {
        assertEquals(1, TipoMovimientoFolio.CARGO.signo());
        assertEquals(-1, TipoMovimientoFolio.PAGO.signo());
        assertEquals(-1, TipoMovimientoFolio.ANULACION_CARGO.signo());
        assertEquals(1, TipoMovimientoFolio.ANULACION_PAGO.signo());
        assertEquals(1, TipoMovimientoFolio.REEMBOLSO.signo());
    }

    @Test
    @DisplayName("un cargo se anula con anulación de cargo y un pago con anulación de pago")
    void contrarioDeCargoYPago() {
        assertEquals(TipoMovimientoFolio.ANULACION_CARGO, TipoMovimientoFolio.CARGO.contrario());
        assertEquals(TipoMovimientoFolio.ANULACION_PAGO, TipoMovimientoFolio.PAGO.contrario());
    }

    @Test
    @DisplayName("las anulaciones y la devolución no se pueden revertir")
    void anulacionesYDevolucionNoTienenContrario() {
        assertThrows(ReglaNegocioExcepcion.class, TipoMovimientoFolio.ANULACION_CARGO::contrario);
        assertThrows(ReglaNegocioExcepcion.class, TipoMovimientoFolio.ANULACION_PAGO::contrario);
        assertThrows(ReglaNegocioExcepcion.class, TipoMovimientoFolio.REEMBOLSO::contrario);
    }

    @Test
    @DisplayName("un pago aparece con monto negativo en el folio")
    void pagoTieneMontoNegativo() {
        MovimientoFolio pago = new MovimientoFolio(1, TipoMovimientoFolio.PAGO, Dinero.de(50_000),
                "Pago en efectivo", LocalDateTime.of(2026, 3, 10, 15, 0), null);
        assertEquals(Dinero.de(-50_000), pago.montoConSigno());
    }
}
