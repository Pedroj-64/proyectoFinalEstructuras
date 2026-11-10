package co.edu.uq.estructuras.sga.core.reservas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.folio.Folio;
import co.edu.uq.estructuras.sga.core.huespedes.Huesped;
import co.edu.uq.estructuras.sga.core.huespedes.PerfilHuesped;
import co.edu.uq.estructuras.sga.core.tarifas.CobroNoche;
import co.edu.uq.estructuras.sga.core.tarifas.Cotizacion;
import co.edu.uq.estructuras.sga.core.tarifas.TarifasTemporada;
import co.edu.uq.estructuras.sga.core.tarifas.Temporada;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.TransicionEstadoInvalidaExcepcion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas del ciclo de vida, el anticipo y el folio de una reserva armada con objetos reales del dominio.
class ReservaTest {

    private static final LocalDate INGRESO = LocalDate.of(2026, 12, 20);
    private static final LocalDate SALIDA = LocalDate.of(2026, 12, 23);
    private static final LocalDateTime CREACION = LocalDateTime.of(2026, 12, 1, 10, 0);
    private static final Dinero TARIFA_BAJA = Dinero.de(100_000);

    /// Apartamento para 4 personas con tarifas de 100.000, 120.000 y 150.000 por persona y noche.
    private static Apartamento apartamento() {
        return new Apartamento("A-101", "Suite A-101", "A", 1, 2, 4, ListaDoblementeEnlazada.de("Wifi"),
                new TarifasTemporada(TARIFA_BAJA, Dinero.de(120_000), Dinero.de(150_000)));
    }

    /// Huésped adulto titular de la reserva.
    private static Huesped titular() {
        return new Huesped(new PerfilHuesped("1094123456", "Ana Gómez", LocalDate.of(1990, 5, 20),
                "3001234567", "ana@correo.com", null));
    }

    /// Cotización en temporada baja para 2 adultos: 200.000 por noche.
    private static Cotizacion cotizacion(Estancia estancia) {
        ListaDoblementeEnlazada<CobroNoche> noches = new ListaDoblementeEnlazada<>();
        Dinero total = Dinero.CERO;
        for (LocalDate noche : estancia) {
            CobroNoche cobro = new CobroNoche(noche, Temporada.BAJA, TARIFA_BAJA, 2, 0, TARIFA_BAJA.veces(2));
            noches.agregarUltimo(cobro);
            total = total.mas(cobro.subtotal());
        }
        return new Cotizacion(estancia, noches, total);
    }

    /// Reserva del 20 al 23 de diciembre (3 noches, 600.000 en total) para el titular y un acompañante.
    private static Reserva reserva(String id, EstadoReserva estadoInicial) {
        Huesped titular = titular();
        Estancia estancia = new Estancia(INGRESO, SALIDA);
        ListaDoblementeEnlazada<Ocupante> ocupantes =
                ListaDoblementeEnlazada.de(titular.comoOcupante(), new Ocupante("Luis Gómez", LocalDate.of(1988, 8, 3)));
        return new Reserva(id, apartamento(), titular, ocupantes, estancia, CanalReserva.PORTAL, estadoInicial,
                CREACION, CREACION.plusHours(Reserva.HORAS_PARA_VENCER_PENDIENTE), LocalTime.of(15, 0),
                cotizacion(estancia));
    }

    @Test
    @DisplayName("el ciclo normal es Pendiente → Confirmada → En curso → Finalizada")
    void cicloNormal() {
        Reserva reserva = reserva("R-0001", EstadoReserva.PENDIENTE);

        reserva.pasarA(EstadoReserva.CONFIRMADA);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.obtenerEstado());
        reserva.pasarA(EstadoReserva.EN_CURSO);
        assertEquals(EstadoReserva.EN_CURSO, reserva.obtenerEstado());
        reserva.pasarA(EstadoReserva.FINALIZADA);
        assertEquals(EstadoReserva.FINALIZADA, reserva.obtenerEstado());
    }

    @Test
    @DisplayName("una transición no permitida lanza excepción y no cambia el estado")
    void transicionNoPermitida() {
        Reserva reserva = reserva("R-0001", EstadoReserva.PENDIENTE);

        assertThrows(TransicionEstadoInvalidaExcepcion.class, () -> reserva.pasarA(EstadoReserva.EN_CURSO));
        assertEquals(EstadoReserva.PENDIENTE, reserva.obtenerEstado());
    }

    @Test
    @DisplayName("el anticipo es el 30 % del total de la cotización")
    void anticipoEsElTreintaPorCiento() {
        Reserva reserva = reserva("R-0001", EstadoReserva.PENDIENTE);

        assertEquals(Dinero.de(600_000), reserva.obtenerCotizacion().total());
        assertEquals(Dinero.de(180_000), reserva.anticipoRequerido());
    }

    @Test
    @DisplayName("se confirma solo cuando lo pagado en el folio alcanza el anticipo")
    void confirmaSoloSiElAnticipoEstaCubierto() {
        Reserva reserva = reserva("R-0001", EstadoReserva.PENDIENTE);
        Folio folio = reserva.obtenerFolio();

        folio.pagar(Dinero.de(100_000), "Primer abono", CREACION.plusHours(1));
        assertFalse(reserva.confirmarSiAnticipoCubierto(), "100.000 no alcanza para un anticipo de 180.000");
        assertEquals(EstadoReserva.PENDIENTE, reserva.obtenerEstado());

        folio.pagar(Dinero.de(80_000), "Segundo abono", CREACION.plusHours(2));
        assertTrue(reserva.confirmarSiAnticipoCubierto(), "180.000 cubre exactamente el anticipo");
        assertEquals(EstadoReserva.CONFIRMADA, reserva.obtenerEstado());

        assertFalse(reserva.confirmarSiAnticipoCubierto(), "una reserva ya confirmada no se vuelve a confirmar");
    }

    @Test
    @DisplayName("solapa con una estancia que comparte noches, no con una que entra el día de salida")
    void solapaConOtraEstancia() {
        Reserva reserva = reserva("R-0001", EstadoReserva.CONFIRMADA);

        assertTrue(reserva.solapa(new Estancia(LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 25))));
        assertFalse(reserva.solapa(new Estancia(SALIDA, LocalDate.of(2026, 12, 26))));
    }

    @Test
    @DisplayName("está activa mientras no termine ni se cancele")
    void estaActiva() {
        Reserva reserva = reserva("R-0001", EstadoReserva.PENDIENTE);
        assertTrue(reserva.estaActiva());

        reserva.pasarA(EstadoReserva.CANCELADA);
        assertFalse(reserva.estaActiva());
    }

    @Test
    @DisplayName("cuenta a todos sus ocupantes, incluido el titular")
    void cantidadOcupantes() {
        assertEquals(2, reserva("R-0001", EstadoReserva.PENDIENTE).cantidadOcupantes());
    }

    @Test
    @DisplayName("el folio nace vacío y con número F- más el id de la reserva")
    void folioNaceConNumero() {
        Folio folio = reserva("R-0001", EstadoReserva.PENDIENTE).obtenerFolio();

        assertEquals("F-R-0001", folio.obtenerNumero());
        assertEquals(0, folio.obtenerMovimientos().tamano());
        assertEquals(Dinero.CERO, folio.saldo());
    }
}
