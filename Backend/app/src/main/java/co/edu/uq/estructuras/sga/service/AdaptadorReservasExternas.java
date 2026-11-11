package co.edu.uq.estructuras.sga.service;

import co.edu.uq.estructuras.sga.core.canal.PuertoReservasExternas;
import co.edu.uq.estructuras.sga.core.canal.SolicitudReservaExterna;
import co.edu.uq.estructuras.sga.core.huespedes.Huesped;
import co.edu.uq.estructuras.sga.core.reservas.CanalReserva;
import co.edu.uq.estructuras.sga.core.reservas.SolicitudReserva;

import org.springframework.stereotype.Component;

/// Conecta el canal externo con el módulo de reservas (patrón Adapter): registra al titular si todavía
/// no existe y crea la reserva por el canal EXTERNO. Así [ServicioCanalExterno] no conoce [ServicioReservas].
@Component
public class AdaptadorReservasExternas implements PuertoReservasExternas {

    private final ServicioHuespedes huespedes;
    private final ServicioReservas reservas;

    /// Recibe por inyección los servicios que se usan para crear la reserva.
    ///
    /// @param huespedes servicio de huéspedes, para buscar o registrar al titular
    /// @param reservas servicio de reservas, que valida y crea la reserva
    public AdaptadorReservasExternas(ServicioHuespedes huespedes, ServicioReservas reservas) {
        this.huespedes = huespedes;
        this.reservas = reservas;
    }

    /// Crea la reserva de la solicitud externa.
    ///
    /// @param solicitud solicitud enviada por la plataforma
    /// @return el id de la reserva creada
    @Override
    public String reservar(SolicitudReservaExterna solicitud) {
        Huesped titular = huespedes.buscarORegistrar(solicitud.titular());
        return reservas.crear(new SolicitudReserva(solicitud.codigoApartamento(), titular.obtenerDocumento(),
                solicitud.ocupantes(), solicitud.estancia(), CanalReserva.EXTERNO, null)).obtenerId();
    }
}
