package co.edu.uq.estructuras.sga.core.tarifas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.core.reservas.Ocupante;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de la liquidación noche por noche de una estancia, con un apartamento real del dominio.
class LiquidadorEstanciaTest {

    private static final LocalDate DIA = LocalDate.of(2026, 3, 10);
    private static final Dinero TARIFA_BAJA = Dinero.de(100_000);
    private static final Dinero TARIFA_MEDIA = Dinero.de(120_000);
    private static final Dinero TARIFA_ALTA = Dinero.de(150_000);

    private static final Ocupante ADULTA = new Ocupante("Ana Gómez", LocalDate.of(1990, 5, 20));
    private static final Ocupante ADULTO = new Ocupante("Luis Gómez", LocalDate.of(1988, 8, 3));

    /// Apartamento para 4 personas con una tarifa distinta por temporada.
    private static Apartamento apartamento() {
        return new Apartamento("A-101", "Suite A-101", "A", 1, 2, 4, ListaDoblementeEnlazada.de("Wifi"),
                new TarifasTemporada(TARIFA_BAJA, TARIFA_MEDIA, TARIFA_ALTA));
    }

    @Test
    @DisplayName("una estancia que cruza de temporada baja a alta cobra cada noche con su tarifa")
    void cruzaDeBajaAAlta() {
        CalendarioTemporadas calendario = new CalendarioTemporadas();
        calendario.agregarPeriodo(new PeriodoTemporada(Temporada.ALTA, DIA.plusDays(1), DIA.plusDays(10)));
        LiquidadorEstancia liquidador = new LiquidadorEstancia(calendario);

        Cotizacion cotizacion = liquidador.cotizacion(apartamento(), new Estancia(DIA, DIA.plusDays(3)),
                ListaDoblementeEnlazada.de(ADULTA, ADULTO));

        ListaDoblementeEnlazada<CobroNoche> noches = cotizacion.noches();
        assertEquals(Temporada.BAJA, noches.obtener(0).temporada());
        assertEquals(TARIFA_BAJA, noches.obtener(0).tarifaPorPersona());
        assertEquals(Dinero.de(200_000), noches.obtener(0).subtotal());
        assertEquals(Temporada.ALTA, noches.obtener(1).temporada());
        assertEquals(TARIFA_ALTA, noches.obtener(1).tarifaPorPersona());
        assertEquals(Dinero.de(300_000), noches.obtener(1).subtotal());
        assertEquals(Temporada.ALTA, noches.obtener(2).temporada());
        assertEquals(Dinero.de(300_000), noches.obtener(2).subtotal());

        Dinero sumaSubtotales = Dinero.CERO;
        for (CobroNoche noche : noches) {
            sumaSubtotales = sumaSubtotales.mas(noche.subtotal());
        }
        assertEquals(sumaSubtotales, cotizacion.total());
        assertEquals(Dinero.de(800_000), cotizacion.total());
    }

    @Test
    @DisplayName("un menor de 6 años cuenta como huésped gratis y no suma al total")
    void menorDeSeisEsGratis() {
        LiquidadorEstancia liquidador = new LiquidadorEstancia(new CalendarioTemporadas());
        Ocupante nino = new Ocupante("Sara Gómez", DIA.minusYears(2));

        Cotizacion cotizacion = liquidador.cotizacion(apartamento(), new Estancia(DIA, DIA.plusDays(1)),
                ListaDoblementeEnlazada.de(ADULTA, nino));

        CobroNoche noche = cotizacion.noches().obtener(0);
        assertEquals(1, noche.huespedesQuePagan());
        assertEquals(1, noche.huespedesGratis());
        assertEquals(TARIFA_BAJA, noche.subtotal());
        assertEquals(TARIFA_BAJA, cotizacion.total());
    }

    @Test
    @DisplayName("el desglose tiene un cobro por noche, en orden")
    void desgloseUnCobroPorNocheEnOrden() {
        LiquidadorEstancia liquidador = new LiquidadorEstancia(new CalendarioTemporadas());
        Estancia estancia = new Estancia(DIA, DIA.plusDays(4));

        Cotizacion cotizacion = liquidador.cotizacion(apartamento(), estancia, ListaDoblementeEnlazada.de(ADULTA));

        assertEquals(estancia, cotizacion.estancia());
        assertEquals(4, cotizacion.noches().tamano());
        for (int i = 0; i < 4; i++) {
            assertEquals(DIA.plusDays(i), cotizacion.noches().obtener(i).noche(), "noche en la posición " + i);
        }
    }

    @Test
    @DisplayName("en temporada baja, el estimado es tarifa × huéspedes × noches")
    void estimadoEnTemporadaBaja() {
        LiquidadorEstancia liquidador = new LiquidadorEstancia(new CalendarioTemporadas());

        Dinero estimado = liquidador.estimado(apartamento(), new Estancia(DIA, DIA.plusDays(4)), 3);

        assertEquals(TARIFA_BAJA.veces(3).veces(4), estimado);
        assertEquals(Dinero.de(1_200_000), estimado);
    }
}
