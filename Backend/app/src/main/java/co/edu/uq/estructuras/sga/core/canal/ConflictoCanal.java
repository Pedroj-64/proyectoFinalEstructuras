package co.edu.uq.estructuras.sga.core.canal;

import java.time.LocalDateTime;

import co.edu.uq.estructuras.sga.core.reservas.Estancia;

/// Solicitud externa rechazada. `idReservaEnConflicto` es `null` si el rechazo no fue por solapamiento.
public record ConflictoCanal(String idSolicitud, String plataforma, String codigoApartamento, Estancia estancia,
                             String motivo, String idReservaEnConflicto, LocalDateTime fechaDeteccion) {
}
