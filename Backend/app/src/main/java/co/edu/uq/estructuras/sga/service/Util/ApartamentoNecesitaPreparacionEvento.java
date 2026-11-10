package co.edu.uq.estructuras.sga.service.Util;

/// Evento de Spring (patrón Observer) que se publica cuando un apartamento pasa a "Pendiente de preparación",
/// por ejemplo después del check-out. Se publica con `ApplicationEventPublisher` y lo reciben los métodos
/// anotados con `@EventListener`.
///
/// @param codigoApartamento código del apartamento que necesita preparación
public record ApartamentoNecesitaPreparacionEvento(String codigoApartamento) {
}
