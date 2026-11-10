package co.edu.uq.estructuras.sga.service.Util;

import co.edu.uq.estructuras.sga.core.reservas.Reserva;

/// Evento de Spring (patrón Observer) que se publica cuando una reserva se crea o deja de estar activa.
/// Se publica con `ApplicationEventPublisher` y lo reciben los métodos anotados con `@EventListener`.
///
/// @param reserva reserva que se creó o dejó de estar activa
public record CambioAgendaReservaEvento(Reserva reserva) {
}
