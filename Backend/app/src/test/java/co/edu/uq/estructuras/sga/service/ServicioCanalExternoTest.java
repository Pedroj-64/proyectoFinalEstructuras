package co.edu.uq.estructuras.sga.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.ReservaSolapadaExcepcion;
import co.edu.uq.estructuras.sga.core.canal.SolicitudReservaExterna;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;

class ServicioCanalExternoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 1, 1, 8, 0);

    private SolicitudReservaExterna solicitud(String id, String apto) {
        return new SolicitudReservaExterna(id, "Booking", apto,
                new Estancia(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 3)), null, new ListaDoblementeEnlazada<>(), AHORA);
    }

    /// Acepta todo salvo el apartamento "OCUPADO", que choca con la reserva R-9.
    private final ServicioCanalExterno canal = new ServicioCanalExterno(s -> {
        if (s.codigoApartamento().equals("OCUPADO")) {
            throw new ReservaSolapadaExcepcion("OCUPADO", s.estancia().toString(), "R-9");
        }
        return "R-" + s.idSolicitud();
    });

    @Test
    @DisplayName("procesa en orden de llegada (FIFO)")
    void fifo() {
        canal.recibir(solicitud("1", "A1"));
        canal.recibir(solicitud("2", "A2"));
        assertEquals("1", canal.procesarSiguiente(AHORA).solicitud().idSolicitud());
        assertEquals("2", canal.procesarSiguiente(AHORA).solicitud().idSolicitud());
        assertNull(canal.procesarSiguiente(AHORA));
    }

    @Test
    @DisplayName("una solicitud aceptada trae el id de su reserva")
    void aceptada() {
        canal.recibir(solicitud("1", "A1"));
        var resultado = canal.procesarSiguiente(AHORA);
        assertTrue(resultado.aceptada());
        assertEquals("R-1", resultado.idReserva());
        assertTrue(canal.conflictos().estaVacia());
    }

    @Test
    @DisplayName("un solapamiento queda como conflicto con la reserva con la que chocó")
    void conflicto() {
        canal.recibir(solicitud("1", "OCUPADO"));
        canal.recibir(solicitud("2", "A2"));
        var resultados = canal.procesarTodas(AHORA);
        assertEquals(2, resultados.tamano());
        assertFalse(resultados.obtener(0).aceptada());
        assertEquals("R-9", canal.conflictos().obtener(0).idReservaEnConflicto());
        assertTrue(resultados.obtener(1).aceptada());
        assertEquals(0, canal.pendientes().tamano());
    }
}
