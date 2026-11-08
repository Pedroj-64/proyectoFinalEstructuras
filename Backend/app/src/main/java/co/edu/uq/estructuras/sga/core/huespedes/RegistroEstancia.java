package co.edu.uq.estructuras.sga.core.huespedes;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;

/// Estancia finalizada que se guarda en el historial del huésped.
public record RegistroEstancia(String idReserva, String codigoApartamento, String nombreApartamento,
                         Estancia estancia, int ocupantes, Dinero total) {
}
