package co.edu.uq.estructuras.sga.service;

import java.time.LocalDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import co.edu.uq.estructuras.sga.estructuras.ColaEnlazada;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.DominioExcepcion;
import co.edu.uq.estructuras.sga.excepciones.ReservaSolapadaExcepcion;
import co.edu.uq.estructuras.sga.core.canal.ConflictoCanal;
import co.edu.uq.estructuras.sga.core.canal.PuertoReservasExternas;
import co.edu.uq.estructuras.sga.core.canal.SolicitudReservaExterna;
import co.edu.uq.estructuras.sga.service.Util.EscenarioReiniciadoEvento;

/// Canal externo: las solicitudes de plataformas esperan en una cola FIFO y se procesan de a una.
/// Si no se pueden reservar quedan registradas como conflicto.
@Service
public class ServicioCanalExterno {

    /// Resultado de procesar una solicitud: `idReserva` si fue aceptada o `conflicto` si no.
    public record ResultadoProcesamiento(SolicitudReservaExterna solicitud, boolean aceptada, String idReserva,
                                         ConflictoCanal conflicto) {
    }

    private final PuertoReservasExternas reservas;
    private final ColaEnlazada<SolicitudReservaExterna> pendientes = new ColaEnlazada<>();
    private final ListaDoblementeEnlazada<ConflictoCanal> conflictos = new ListaDoblementeEnlazada<>();

    /// Recibe por inyección el puerto con el que se crean las reservas.
    public ServicioCanalExterno(PuertoReservasExternas reservas) {
        this.reservas = reservas;
    }

    /// Observer: vacía la cola y los conflictos antes de cargar un escenario nuevo.
    @EventListener
    public void alReiniciarEscenario(EscenarioReiniciadoEvento evento) {
        pendientes.vaciar();
        conflictos.vaciar();
    }

    /// Una plataforma envía una solicitud: queda al final de la cola.
    public void recibir(SolicitudReservaExterna solicitud) {
        pendientes.encolar(solicitud);
    }

    /// Procesa la solicitud más antigua; devuelve `null` si la cola está vacía.
    public ResultadoProcesamiento procesarSiguiente(LocalDateTime ahora) {
        if (pendientes.estaVacia()) {
            return null;
        }
        SolicitudReservaExterna solicitud = pendientes.desencolar();
        try {
            return new ResultadoProcesamiento(solicitud, true, reservas.reservar(solicitud), null);
        } catch (DominioExcepcion e) {
            String enConflicto = e instanceof ReservaSolapadaExcepcion s ? s.obtenerIdReservaEnConflicto() : null;
            ConflictoCanal conflicto = new ConflictoCanal(solicitud.idSolicitud(), solicitud.plataforma(),
                    solicitud.codigoApartamento(), solicitud.estancia(), e.getMessage(), enConflicto, ahora);
            conflictos.agregarUltimo(conflicto);
            return new ResultadoProcesamiento(solicitud, false, null, conflicto);
        }
    }

    /// Procesa toda la cola en orden de llegada.
    public ListaDoblementeEnlazada<ResultadoProcesamiento> procesarTodas(LocalDateTime ahora) {
        ListaDoblementeEnlazada<ResultadoProcesamiento> resultados = new ListaDoblementeEnlazada<>();
        while (!pendientes.estaVacia()) {
            resultados.agregarUltimo(procesarSiguiente(ahora));
        }
        return resultados;
    }

    /// Solicitudes que esperan, de la más antigua a la más reciente.
    public ListaDoblementeEnlazada<SolicitudReservaExterna> pendientes() {
        return ListaDoblementeEnlazada.copiarDe(pendientes);
    }

    /// Conflictos registrados, en orden de detección.
    public ListaDoblementeEnlazada<ConflictoCanal> conflictos() {
        return ListaDoblementeEnlazada.copiarDe(conflictos);
    }
}
