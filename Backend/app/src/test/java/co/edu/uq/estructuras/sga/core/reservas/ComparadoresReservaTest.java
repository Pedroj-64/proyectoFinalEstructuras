package co.edu.uq.estructuras.sga.core.reservas;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.huespedes.Huesped;
import co.edu.uq.estructuras.sga.core.huespedes.PerfilHuesped;
import co.edu.uq.estructuras.sga.core.tarifas.CobroNoche;
import co.edu.uq.estructuras.sga.core.tarifas.Cotizacion;
import co.edu.uq.estructuras.sga.core.tarifas.TarifasTemporada;
import co.edu.uq.estructuras.sga.core.tarifas.Temporada;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de los órdenes alternativos de reservas, con reservas armadas con objetos reales del dominio.
class ComparadoresReservaTest {

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

    /// Reserva del 20 al 23 de diciembre con las fechas de creación y vencimiento dadas.
    /// Si no vence (`vencimiento == null`), nace confirmada, como una reserva directa; si vence, nace pendiente.
    private static Reserva reserva(String id, LocalDateTime creacion, LocalDateTime vencimiento) {
        Huesped titular = titular();
        Estancia estancia = new Estancia(INGRESO, SALIDA);
        ListaDoblementeEnlazada<Ocupante> ocupantes =
                ListaDoblementeEnlazada.de(titular.comoOcupante(), new Ocupante("Luis Gómez", LocalDate.of(1988, 8, 3)));
        EstadoReserva estado = vencimiento == null ? EstadoReserva.CONFIRMADA : EstadoReserva.PENDIENTE;
        CanalReserva canal = vencimiento == null ? CanalReserva.DIRECTO : CanalReserva.PORTAL;
        return new Reserva(id, apartamento(), titular, ocupantes, estancia, canal, estado,
                creacion, vencimiento, LocalTime.of(15, 0), cotizacion(estancia));
    }

    /// Ids de las reservas en el orden de la lista.
    private static String[] ids(ListaDoblementeEnlazada<Reserva> reservas) {
        String[] ids = new String[reservas.tamano()];
        int i = 0;
        for (Reserva reserva : reservas) {
            ids[i++] = reserva.obtenerId();
        }
        return ids;
    }

    @Test
    @DisplayName("porVencimiento ordena de la que vence primero a la última y deja al final las que no vencen")
    void porVencimientoDeLaPrimeraALaUltima() {
        ListaDoblementeEnlazada<Reserva> reservas = ListaDoblementeEnlazada.de(
                reserva("R-1", CREACION, null),
                reserva("R-2", CREACION, CREACION.plusHours(24)),
                reserva("R-3", CREACION, CREACION.plusHours(6)),
                reserva("R-4", CREACION, CREACION.plusHours(12)));

        assertArrayEquals(new String[] {"R-3", "R-4", "R-2", "R-1"},
                ids(reservas.ordenada(ComparadoresReserva.porVencimiento())));
    }

    @Test
    @DisplayName("porVencimiento desempata por id, también entre las que no vencen")
    void porVencimientoDesempataPorId() {
        LocalDateTime vence = CREACION.plusHours(24);
        // Entran en orden inverso de id para que el resultado no dependa de que el ordenamiento sea estable.
        ListaDoblementeEnlazada<Reserva> reservas = ListaDoblementeEnlazada.de(
                reserva("R-9", CREACION, null),
                reserva("R-7", CREACION, vence),
                reserva("R-5", CREACION, null),
                reserva("R-6", CREACION, vence));

        assertArrayEquals(new String[] {"R-6", "R-7", "R-5", "R-9"},
                ids(reservas.ordenada(ComparadoresReserva.porVencimiento())));
    }

    @Test
    @DisplayName("porCreacionDescendente pone primero la más reciente")
    void porCreacionDescendenteLaMasRecientePrimero() {
        ListaDoblementeEnlazada<Reserva> reservas = ListaDoblementeEnlazada.de(
                reserva("R-1", CREACION, null),
                reserva("R-2", CREACION.plusDays(2), null),
                reserva("R-3", CREACION.plusDays(1), null));

        assertArrayEquals(new String[] {"R-2", "R-3", "R-1"},
                ids(reservas.ordenada(ComparadoresReserva.porCreacionDescendente())));
    }

    @Test
    @DisplayName("porCreacionDescendente desempata por id en orden ascendente")
    void porCreacionDescendenteDesempataPorId() {
        // R-8 y R-2 se crearon al mismo tiempo; entran en orden inverso de id.
        ListaDoblementeEnlazada<Reserva> reservas = ListaDoblementeEnlazada.de(
                reserva("R-8", CREACION.plusDays(1), null),
                reserva("R-5", CREACION, null),
                reserva("R-2", CREACION.plusDays(1), null));

        assertArrayEquals(new String[] {"R-2", "R-8", "R-5"},
                ids(reservas.ordenada(ComparadoresReserva.porCreacionDescendente())));
    }
}
