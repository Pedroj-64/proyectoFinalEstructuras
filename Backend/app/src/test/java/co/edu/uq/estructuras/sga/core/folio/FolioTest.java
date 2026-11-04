package co.edu.uq.estructuras.sga.core.folio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas del saldo del folio, de cómo se deshacen las operaciones de recepción y de la validación de montos.
class FolioTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 3, 10, 15, 0);

    @Test
    @DisplayName("el saldo suma los cargos y resta los pagos")
    void saldoSumaCargosYRestaPagos() {
        Folio folio = new Folio("F-001");
        folio.cargo(Dinero.de(100_000), "Alojamiento", AHORA);
        folio.cargo(Dinero.de(20_000), "Minibar", AHORA);
        folio.pagar(Dinero.de(50_000), "Abono en efectivo", AHORA);

        assertEquals(Dinero.de(70_000), folio.saldo());
    }

    @Test
    @DisplayName("deshacer no borra el movimiento: registra su contrario")
    void deshacerRegistraElContrario() {
        Folio folio = new Folio("F-001");
        MovimientoFolio cargo = folio.cargo(Dinero.de(20_000), "Minibar", AHORA);

        MovimientoFolio anulacion = folio.deshacerUltimo(AHORA.plusHours(1));

        ListaDoblementeEnlazada<MovimientoFolio> movimientos = folio.obtenerMovimientos();
        assertEquals(2, movimientos.tamano());
        assertEquals(cargo, movimientos.obtener(0));
        assertEquals(anulacion, movimientos.obtener(1));
        assertEquals(TipoMovimientoFolio.ANULACION_CARGO, anulacion.tipo());
        assertEquals(cargo.monto(), anulacion.monto());
        assertEquals(Integer.valueOf(cargo.numero()), anulacion.anulaMovimiento());
        assertEquals(Dinero.CERO, folio.saldo());
    }

    @Test
    @DisplayName("se deshace en orden inverso hasta que no queda nada por deshacer")
    void deshaceEnOrdenInverso() {
        Folio folio = new Folio("F-001");
        MovimientoFolio primero = folio.cargo(Dinero.de(100_000), "Alojamiento", AHORA);
        MovimientoFolio segundo = folio.pagar(Dinero.de(50_000), "Abono", AHORA);
        MovimientoFolio tercero = folio.cargo(Dinero.de(20_000), "Minibar", AHORA);

        assertTrue(folio.puedeDeshacer());
        assertEquals(Integer.valueOf(tercero.numero()), folio.deshacerUltimo(AHORA).anulaMovimiento());
        assertEquals(Integer.valueOf(segundo.numero()), folio.deshacerUltimo(AHORA).anulaMovimiento());
        assertEquals(Integer.valueOf(primero.numero()), folio.deshacerUltimo(AHORA).anulaMovimiento());

        assertFalse(folio.puedeDeshacer());
        assertThrows(ReglaNegocioExcepcion.class, () -> folio.deshacerUltimo(AHORA));
        assertEquals(Dinero.CERO, folio.saldo());
    }

    @Test
    @DisplayName("los movimientos del sistema no se pueden deshacer")
    void movimientosDelSistemaNoSeDeshacen() {
        Folio folio = new Folio("F-001");
        folio.registrarMovimientoSistema(TipoMovimientoFolio.CARGO, Dinero.de(300_000), "Alojamiento 3 noches", AHORA);

        assertFalse(folio.puedeDeshacer());
        assertNull(folio.proximoDeshacible());
        assertThrows(ReglaNegocioExcepcion.class, () -> folio.deshacerUltimo(AHORA));

        MovimientoFolio minibar = folio.cargo(Dinero.de(20_000), "Minibar", AHORA);
        folio.registrarMovimientoSistema(TipoMovimientoFolio.REEMBOLSO, Dinero.de(10_000), "Devolución", AHORA);

        MovimientoFolio deshecho = folio.deshacerUltimo(AHORA);
        assertEquals(Integer.valueOf(minibar.numero()), deshecho.anulaMovimiento(),
                "deshacer debe saltarse el reembolso del sistema y anular el cargo de recepción");
        assertFalse(folio.puedeDeshacer());
    }

    @Test
    @DisplayName("los montos en cero o negativos se rechazan y no quedan registrados")
    void montosNoPositivos() {
        Folio folio = new Folio("F-001");

        assertThrows(ReglaNegocioExcepcion.class, () -> folio.cargo(Dinero.CERO, "Minibar", AHORA));
        assertThrows(ReglaNegocioExcepcion.class, () -> folio.cargo(Dinero.de(-5_000), "Minibar", AHORA));
        assertThrows(ReglaNegocioExcepcion.class, () -> folio.pagar(Dinero.CERO, "Abono", AHORA));
        assertThrows(ReglaNegocioExcepcion.class, () -> folio.pagar(Dinero.de(-5_000), "Abono", AHORA));
        assertThrows(ReglaNegocioExcepcion.class,
                () -> folio.registrarMovimientoSistema(TipoMovimientoFolio.CARGO, Dinero.CERO, "Alojamiento", AHORA));
        assertThrows(ReglaNegocioExcepcion.class, () -> folio.cargo(null, "Minibar", AHORA));

        assertEquals(0, folio.obtenerMovimientos().tamano());
        assertFalse(folio.puedeDeshacer());
    }
}
