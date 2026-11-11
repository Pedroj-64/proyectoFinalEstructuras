package co.edu.uq.estructuras.sga.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.huespedes.Huesped;
import co.edu.uq.estructuras.sga.core.huespedes.PerfilHuesped;
import co.edu.uq.estructuras.sga.core.reservas.CanalReserva;
import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.reservas.EstadoReserva;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.core.reservas.Ocupante;
import co.edu.uq.estructuras.sga.core.reservas.Reserva;
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

/// Pruebas del repositorio de reservas: un árbol por apartamento ordenado por fecha de entrada.
class RepositorioReservasMemoriaTest {

    private static final Apartamento APARTAMENTO = new Apartamento("A-101", "Suite", "A", 1, 2, 4,
            ListaDoblementeEnlazada.de("Wifi"), new TarifasTemporada(Dinero.de(100_000), Dinero.de(120_000), Dinero.de(150_000)));
    private static final Huesped TITULAR = new Huesped(new PerfilHuesped("123", "Ana", LocalDate.of(1990, 1, 1),
            "300", "ana@uq.edu.co", null));

    private static Estancia estancia(int diaIngreso, int diaSalida) {
        return new Estancia(LocalDate.of(2026, 12, diaIngreso), LocalDate.of(2026, 12, diaSalida));
    }

    private static Reserva reserva(String id, Estancia estancia, EstadoReserva estado) {
        ListaDoblementeEnlazada<CobroNoche> noches = new ListaDoblementeEnlazada<>();
        Dinero total = Dinero.CERO;
        for (LocalDate noche : estancia) {
            CobroNoche cobro = new CobroNoche(noche, Temporada.BAJA, Dinero.de(100_000), 1, 0, Dinero.de(100_000));
            noches.agregarUltimo(cobro);
            total = total.mas(cobro.subtotal());
        }
        Cotizacion cotizacion = new Cotizacion(estancia, noches, total);
        return new Reserva(id, APARTAMENTO, TITULAR, ListaDoblementeEnlazada.de(new Ocupante("Ana", LocalDate.of(1990, 1, 1))),
                estancia, CanalReserva.DIRECTO, estado, LocalDateTime.of(2026, 12, 1, 10, 0), null, LocalTime.of(15, 0), cotizacion);
    }

    @Test
    @DisplayName("genera ids distintos y busca por id")
    void idsYBusqueda() {
        RepositorioReservasMemoria repositorio = new RepositorioReservasMemoria();
        assertNotEquals(repositorio.siguienteId(), repositorio.siguienteId());
        Reserva reserva = reserva("R-1", estancia(10, 12), EstadoReserva.CONFIRMADA);
        repositorio.guardar(reserva);
        assertSame(reserva, repositorio.buscarPorId("R-1"));
        assertNull(repositorio.buscarPorId("R-9"));
    }

    @Test
    @DisplayName("detecta solapamientos: la noche de salida no cuenta y las canceladas se ignoran")
    void solapamientos() {
        RepositorioReservasMemoria repositorio = new RepositorioReservasMemoria();
        repositorio.guardar(reserva("R-1", estancia(10, 12), EstadoReserva.CONFIRMADA));
        repositorio.guardar(reserva("R-2", estancia(20, 25), EstadoReserva.CANCELADA));

        assertEquals("R-1", repositorio.buscarSolapada("A-101", estancia(11, 14)).obtenerId());
        assertNull(repositorio.buscarSolapada("A-101", estancia(12, 15)));
        assertNull(repositorio.buscarSolapada("A-101", estancia(21, 23)));
        assertEquals(1, repositorio.buscarQueSeCruzan("A-101", estancia(21, 23)).tamano());
    }

    @Test
    @DisplayName("lista las reservas de un apartamento en orden de entrada y las cuenta")
    void ordenYConteo() {
        RepositorioReservasMemoria repositorio = new RepositorioReservasMemoria();
        repositorio.guardar(reserva("R-2", estancia(20, 22), EstadoReserva.CONFIRMADA));
        repositorio.guardar(reserva("R-1", estancia(5, 7), EstadoReserva.CONFIRMADA));
        assertEquals("R-1", repositorio.buscarPorApartamento("A-101").obtenerPrimero().obtenerId());
        assertEquals(2, repositorio.contarPorApartamento("A-101"));
        repositorio.vaciar();
        assertEquals(0, repositorio.buscarTodos().tamano());
    }
}
