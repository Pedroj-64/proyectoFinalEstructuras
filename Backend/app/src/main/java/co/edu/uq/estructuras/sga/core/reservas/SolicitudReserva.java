package co.edu.uq.estructuras.sga.core.reservas;

import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;

import java.time.LocalTime;

/// Solicitud para reservar un apartamento.
///
/// @param codigoApartamento código del apartamento que se quiere reservar
/// @param documentoTitular documento de identidad del titular de la reserva
/// @param ocupantes personas que se hospedarán en el apartamento
/// @param estancia fechas de entrada y salida
/// @param canal canal por el que llega la solicitud
/// @param llegadaEstimada hora estimada de llegada del huésped
public record SolicitudReserva(String codigoApartamento, String documentoTitular, ListaDoblementeEnlazada<Ocupante> ocupantes,
                                 Estancia estancia, CanalReserva canal, LocalTime llegadaEstimada) {
}
