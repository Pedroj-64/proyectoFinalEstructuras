package co.edu.uq.estructuras.sga.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uq.estructuras.sga.core.personal.Empleado;
import co.edu.uq.estructuras.sga.core.personal.RolEmpleado;
import co.edu.uq.estructuras.sga.core.tiempo.ProveedorTiempo;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de la rotación de turnos sobre la lista circular de recepcionistas.
class ServicioTurnosTest {

    /// Reloj fijo en la fecha y hora indicadas.
    private static ProveedorTiempo reloj(LocalDateTime ahora) {
        return () -> ahora;
    }

    /// Recepcionistas de apoyo en el orden en que rotan.
    private static ListaDoblementeEnlazada<Empleado> recepcionistas() {
        return ListaDoblementeEnlazada.de(
                new Empleado("101", "Ana", RolEmpleado.RECEPCIONISTA, null, null),
                new Empleado("102", "Beto", RolEmpleado.RECEPCIONISTA, null, null),
                new Empleado("103", "Carla", RolEmpleado.RECEPCIONISTA, null, null));
    }

    @Test
    @DisplayName("asigna el turno de la mañana al primer recepcionista de la rotación")
    void turnoActualEnLaManana() {
        ServicioTurnos servicio = new ServicioTurnos(reloj(LocalDateTime.of(2026, 11, 10, 9, 0)));
        ServicioTurnos.TurnoAsignado turno = servicio.actual(recepcionistas());

        assertEquals("101", turno.recepcionista().obtenerId());
        assertEquals(ServicioTurnos.FranjaTurno.MANANA, turno.franja());
        assertEquals(LocalDateTime.of(2026, 11, 10, 6, 0), turno.inicio());
        assertEquals(LocalDateTime.of(2026, 11, 10, 14, 0), turno.fin());
    }

    @Test
    @DisplayName("rota los recepcionistas en las tres franjas y vuelve al primero al día siguiente")
    void rotaEnListaCircular() {
        assertEquals("102", new ServicioTurnos(reloj(LocalDateTime.of(2026, 11, 10, 15, 0)))
                .actual(recepcionistas()).recepcionista().obtenerId());
        assertEquals("103", new ServicioTurnos(reloj(LocalDateTime.of(2026, 11, 10, 23, 0)))
                .actual(recepcionistas()).recepcionista().obtenerId());
        assertEquals("101", new ServicioTurnos(reloj(LocalDateTime.of(2026, 11, 11, 7, 0)))
                .actual(recepcionistas()).recepcionista().obtenerId());
    }

    @Test
    @DisplayName("antes de las 06:00 sigue vigente el turno de noche del día anterior")
    void madrugadaPerteneceAlTurnoDeNoche() {
        ServicioTurnos servicio = new ServicioTurnos(reloj(LocalDateTime.of(2026, 11, 11, 3, 0)));
        ServicioTurnos.TurnoAsignado turno = servicio.actual(recepcionistas());

        assertEquals(ServicioTurnos.FranjaTurno.NOCHE, turno.franja());
        assertEquals(LocalDateTime.of(2026, 11, 10, 22, 0), turno.inicio());
        assertEquals("103", turno.recepcionista().obtenerId());
    }

    @Test
    @DisplayName("proximos entrega el turno actual y los siguientes en orden")
    void listaProximosTurnos() {
        ServicioTurnos servicio = new ServicioTurnos(reloj(LocalDateTime.of(2026, 11, 10, 9, 0)));

        var turnos = servicio.proximos(recepcionistas(), 4);

        assertEquals(4, turnos.tamano());
        assertEquals(ServicioTurnos.FranjaTurno.MANANA, turnos.obtener(0).franja());
        assertEquals(ServicioTurnos.FranjaTurno.TARDE, turnos.obtener(1).franja());
        assertEquals(ServicioTurnos.FranjaTurno.NOCHE, turnos.obtener(2).franja());
        assertEquals(ServicioTurnos.FranjaTurno.MANANA, turnos.obtener(3).franja());
        assertEquals("101", turnos.obtener(0).recepcionista().obtenerId());
        assertEquals("102", turnos.obtener(1).recepcionista().obtenerId());
        assertEquals("103", turnos.obtener(2).recepcionista().obtenerId());
        assertEquals("101", turnos.obtener(3).recepcionista().obtenerId());
    }

    @Test
    @DisplayName("falla si no hay recepcionistas para asignar turnos")
    void exigeRecepcionistas() {
        ServicioTurnos servicio = new ServicioTurnos(reloj(LocalDateTime.of(2026, 11, 10, 9, 0)));

        assertThrows(ReglaNegocioExcepcion.class,
                () -> servicio.actual(new ListaDoblementeEnlazada<>()));
    }
}
