package co.edu.uq.estructuras.sga.service;

import co.edu.uq.estructuras.sga.core.reservas.ComparadoresReserva;
import co.edu.uq.estructuras.sga.core.reservas.EstadoReserva;
import co.edu.uq.estructuras.sga.core.reservas.Reserva;
import co.edu.uq.estructuras.sga.estructuras.ColaPrioridadMonticulo;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.service.Util.CambioAgendaReservaEvento;
import co.edu.uq.estructuras.sga.service.Util.EscenarioReiniciadoEvento;
import co.edu.uq.estructuras.sga.service.tiempo.ProveedorTiempo;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/// Vencimiento automático de las reservas PENDIENTES que no pagan el anticipo a tiempo.
///
/// Las reservas pendientes se guardan en una cola de prioridad (montículo) ordenada por hora de vencimiento,
/// así revisar cuáles vencieron solo mira el frente de la cola y no todas las reservas. La cola se llena
/// escuchando el [CambioAgendaReservaEvento] (patrón Observer).
@Service
public class ServicioVencimientos {

    private final ServicioReservas reservas;
    private final ProveedorTiempo tiempo;
    /// Reservas pendientes ordenadas por fecha de vencimiento; el frente es la que vence primero.
    private final ColaPrioridadMonticulo<Reserva> pendientesPorVencimiento =
            new ColaPrioridadMonticulo<>(ComparadoresReserva.porVencimiento());

    /// Recibe por inyección el servicio de reservas y el reloj.
    ///
    /// @param reservas servicio que vence las reservas
    /// @param tiempo reloj del sistema
    public ServicioVencimientos(ServicioReservas reservas, ProveedorTiempo tiempo) {
        this.reservas = reservas;
        this.tiempo = tiempo;
    }

    /// Observer: cuando una reserva se crea o cambia, la agrega a la cola si está PENDIENTE, tiene fecha de
    /// vencimiento y no estaba ya en la cola. No retira reservas: las que se confirman o cancelan salen de
    /// la cola cuando llegan al frente en [#vencerVencidas()].
    ///
    /// @param evento evento con la reserva que cambió
    @EventListener
    public void alCambiarReserva(CambioAgendaReservaEvento evento) {
        Reserva reserva = evento.reserva();
        if (reserva.obtenerEstado() == EstadoReserva.PENDIENTE && reserva.obtenerFechaVencimiento() != null
                && !pendientesPorVencimiento.matchAny(reserva::equals)) {
            pendientesPorVencimiento.ofrecer(reserva);
        }
    }

    /// Observer: vacía la cola antes de cargar un escenario nuevo.
    ///
    /// @param evento evento de reinicio del escenario
    @EventListener
    public void alReiniciarEscenario(EscenarioReiniciadoEvento evento) {
        pendientesPorVencimiento.vaciar();
    }

    /// Caso de uso: cancelar cada reserva pendiente cuyo plazo ya pasó. Saca de la cola, en orden, las
    /// reservas con vencimiento igual o anterior a ahora; las que se confirmaron o cancelaron mientras
    /// tanto solo salen de la cola.
    ///
    /// @return los id de las reservas que se vencieron
    public ListaDoblementeEnlazada<String> vencerVencidas() {
        ListaDoblementeEnlazada<String> vencida = new ListaDoblementeEnlazada<>();
        while (!pendientesPorVencimiento.estaVacia() && !pendientesPorVencimiento.ver().obtenerFechaVencimiento().isAfter(tiempo.ahora())) {
            Reserva reserva = pendientesPorVencimiento.sacar();
            // ¿Se confirmó o canceló mientras tanto? Entonces simplemente sale de la cola.
            if (reservas.vencer(reserva.obtenerId())) {
                vencida.agregarUltimo(reserva.obtenerId());
            }
        }
        return vencida;
    }

    /// Caso de uso: ver las reservas que siguen esperando el anticipo, primero las que vencen antes.
    ///
    /// @return las reservas de la cola que todavía están PENDIENTES, ordenadas por vencimiento
    public ListaDoblementeEnlazada<Reserva> proximos() {
        return pendientesPorVencimiento.aListaOrdenada()
                .filtrar(reserva -> reserva.obtenerEstado() == EstadoReserva.PENDIENTE);
    }
}
