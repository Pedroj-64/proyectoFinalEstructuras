package co.edu.uq.estructuras.sga.core.apartamentos;

import co.edu.uq.estructuras.sga.core.tarifas.Temporada;


import java.util.Comparator;
import java.util.function.ToIntFunction;

/// Órdenes alternativos para apartamentos (el orden natural, por código, está en [Apartamento]).
public final class ComparadoresApartamento {

    /// Clase de utilidades: no se instancia.
    private ComparadoresApartamento() {
    }

    /// Primero la tarifa por persona más barata, según la temporada de la primera noche; empates por código.
    public static Comparator<Apartamento> porPrecio(Temporada temporada) {
        return Comparator.<Apartamento>comparingLong(apartamento -> apartamento.tarifaPara(temporada).monto())
                .thenComparing(Comparator.naturalOrder());
    }

    /// Primero la mayor capacidad; empates por código.
    public static Comparator<Apartamento> porCapacidad() {
        return Comparator.comparingInt(Apartamento::obtenerCapacidad).reversed()
                .thenComparing(Comparator.naturalOrder());
    }

    /// Primero más habitaciones; empates por código.
    public static Comparator<Apartamento> porHabitaciones() {
        return Comparator.comparingInt(Apartamento::obtenerHabitaciones).reversed()
                .thenComparing(Comparator.naturalOrder());
    }

    /// Primero los más reservados. El contador se inyecta porque las reservas viven en su propio repositorio.
    public static Comparator<Apartamento> porCantidadReservas(ToIntFunction<Apartamento> contadorReservas) {
        return Comparator.comparingInt(contadorReservas).reversed()
                .thenComparing(Comparator.naturalOrder());
    }
}
