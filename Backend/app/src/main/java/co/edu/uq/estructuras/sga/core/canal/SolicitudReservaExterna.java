package co.edu.uq.estructuras.sga.core.canal;

import java.time.LocalDateTime;

import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.core.huespedes.PerfilHuesped;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.core.reservas.Ocupante;

/// Solicitud de reserva que llega desde una plataforma externa y espera en la cola.
public record SolicitudReservaExterna(String idSolicitud, String plataforma, String codigoApartamento, Estancia estancia,
                                      PerfilHuesped titular, ListaDoblementeEnlazada<Ocupante> ocupantes,
                                      LocalDateTime fechaRecepcion) {
}
