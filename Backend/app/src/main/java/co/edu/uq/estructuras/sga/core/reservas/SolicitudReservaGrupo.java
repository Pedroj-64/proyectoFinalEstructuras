package co.edu.uq.estructuras.sga.core.reservas;

import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;

import java.time.LocalTime;

/// Solicitud de un grupo que puede necesitar varios apartamentos.
///
/// @param documentoTitular documento de identidad del titular de la reserva
/// @param ocupantes todas las personas del grupo
/// @param estancia fechas de entrada y salida
/// @param canal canal por el que llega la solicitud
/// @param llegadaEstimada hora estimada de llegada del grupo
public record SolicitudReservaGrupo(String documentoTitular, ListaDoblementeEnlazada<Ocupante> ocupantes, Estancia estancia,
                                      CanalReserva canal, LocalTime llegadaEstimada) {
}
