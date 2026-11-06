package co.edu.uq.estructuras.sga.core.tarifas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de que una cotización y el cobro de cada noche conservan los datos con los que se crean.
class CotizacionTest {

    private static final LocalDate DIA_10 = LocalDate.of(2026, 3, 10);
    private static final LocalDate DIA_11 = LocalDate.of(2026, 3, 11);

    @Test
    @DisplayName("una cotización de 2 noches guarda la estancia, las noches en orden y el total")
    void cotizacionDeDosNoches() {
        Estancia estancia = new Estancia(DIA_10, DIA_10.plusDays(2));
        CobroNoche primera = new CobroNoche(DIA_10, Temporada.BAJA, Dinero.de(80_000), 2, 1, Dinero.de(160_000));
        CobroNoche segunda = new CobroNoche(DIA_11, Temporada.ALTA, Dinero.de(120_000), 2, 1, Dinero.de(240_000));

        Cotizacion cotizacion = new Cotizacion(estancia, ListaDoblementeEnlazada.de(primera, segunda),
                Dinero.de(400_000));

        assertEquals(estancia, cotizacion.estancia());
        assertEquals(2, cotizacion.noches().tamano());
        assertEquals(primera, cotizacion.noches().obtener(0));
        assertEquals(segunda, cotizacion.noches().obtener(1));
        assertEquals(Dinero.de(400_000), cotizacion.total());
    }

    @Test
    @DisplayName("el cobro de una noche guarda los huéspedes que pagan, los gratis y el subtotal")
    void cobroNocheGuardaSusDatos() {
        CobroNoche cobro = new CobroNoche(DIA_10, Temporada.MEDIA, Dinero.de(100_000), 2, 1, Dinero.de(200_000));

        assertEquals(2, cobro.huespedesQuePagan());
        assertEquals(1, cobro.huespedesGratis());
        assertEquals(Dinero.de(200_000), cobro.subtotal());
    }
}
