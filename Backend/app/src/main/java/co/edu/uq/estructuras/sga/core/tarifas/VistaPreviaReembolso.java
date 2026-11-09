package co.edu.uq.estructuras.sga.core.tarifas;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;

/// Lo que se le devolvería al huésped si la reserva se cancelara ahora.
///
/// @param idReserva identificador de la reserva
/// @param pagado lo que el huésped ha pagado hasta ahora
/// @param porcentaje porcentaje de lo pagado que se devolvería, de 0 a 100
/// @param reembolso valor que se le devolvería al huésped
/// @param penalidad parte de lo pagado que no se devolvería
/// @param diasAntesIngreso días que faltan para la fecha de entrada
public record VistaPreviaReembolso(String idReserva, Dinero pagado, int porcentaje, Dinero reembolso, Dinero penalidad,
                            long diasAntesIngreso) {
}
