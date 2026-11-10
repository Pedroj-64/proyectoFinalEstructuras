package co.edu.uq.estructuras.sga.core.reservas;

import java.util.Comparator;

/// Órdenes alternativos para reservas (el orden natural, por fecha de entrada, está en [Reserva]).
public final class ComparadoresReserva {

    /// Clase de utilidades: no se instancia.
    private ComparadoresReserva() {
    }

    /// Ordena por fecha de vencimiento, de la que vence primero a la que vence de última.
    /// Las reservas sin fecha de vencimiento (`null`) van al final; si dos vencen al mismo tiempo, se desempata por id.
    ///
    /// @return comparador por fecha de vencimiento ascendente y luego por id
    public static Comparator<Reserva> porVencimiento() {
        return Comparator.comparing(Reserva::obtenerFechaVencimiento, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Reserva::obtenerId);
    }

    /// Ordena por fecha de creación, de la más reciente a la más antigua; si dos se crearon al mismo tiempo,
    /// se desempata por id en orden ascendente.
    ///
    /// @return comparador por fecha de creación descendente y luego por id
    public static Comparator<Reserva> porCreacionDescendente() {
        return Comparator.comparing(Reserva::obtenerFechaCreacion).reversed().thenComparing(Reserva::obtenerId);
    }
}
