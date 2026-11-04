package co.edu.uq.estructuras.sga.core.reservas;

import co.edu.uq.estructuras.sga.excepciones.RangoFechasInvalidoExcepcion;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.NoSuchElementException;

/// Estancia desde `ingreso` (primera noche) hasta `salida` (día de salida, que no se cobra ni se ocupa).
///
/// Es [Iterable]: `for (LocalDate noche : estancia)` recorre cada noche de la estancia, así que
/// del 10 al 12 de marzo entrega el 10 y el 11 de marzo.
public record Estancia(LocalDate ingreso, LocalDate salida) implements Iterable<LocalDate> {

    public Estancia {
        if (ingreso == null || salida == null) {
            throw new RangoFechasInvalidoExcepcion("Las fechas de entrada y salida son obligatorias");
        }
        if (!salida.isAfter(ingreso)) {
            throw new RangoFechasInvalidoExcepcion(
                    "La fecha de salida (" + salida + ") debe ser posterior a la de entrada (" + ingreso + ")");
        }
    }

    /// Cantidad de noches de la estancia.
    public long noches() {
        return ChronoUnit.DAYS.between(ingreso, salida);
    }

    /// Dos estancias se solapan cuando comparten al menos una noche.
    public boolean solapa(Estancia otro) {
        return ingreso.isBefore(otro.salida) && otro.ingreso.isBefore(salida);
    }

    /// Indica si la noche pertenece a la estancia.
    public boolean incluyeNoche(LocalDate noche) {
        return !noche.isBefore(ingreso) && noche.isBefore(salida);
    }

    /// Iterador propio que entrega cada noche desde la entrada hasta el día anterior a la salida.
    @Override
    public Iterator<LocalDate> iterator() {
        return new Iterator<>() {
            /// Siguiente noche que devolverá el iterador.
            private LocalDate siguiente = ingreso;

            /// Hay otra noche mientras no se llegue a la fecha de salida.
            @Override
            public boolean hasNext() {
                return siguiente.isBefore(salida);
            }

            /// Devuelve la noche actual y avanza un día.
            @Override
            public LocalDate next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                LocalDate noche = siguiente;
                siguiente = siguiente.plusDays(1);
                return noche;
            }
        };
    }

    /// Formato `entrada → salida`.
    @Override
    public String toString() {
        return ingreso + " → " + salida;
    }
}
