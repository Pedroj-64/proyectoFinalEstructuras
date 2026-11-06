package co.edu.uq.estructuras.sga.core.tarifas;

import co.edu.uq.estructuras.sga.excepciones.RangoFechasInvalidoExcepcion;

import java.time.LocalDate;

/// Rango de fechas (ambas incluidas) que pertenece a una temporada.
public record PeriodoTemporada(Temporada temporada, LocalDate inicio, LocalDate fin) {

    /// Valida que la temporada y las dos fechas existan y que `fin` no sea anterior a `inicio`.
    ///
    /// @throws RangoFechasInvalidoExcepcion si falta la temporada o alguna fecha, o si `fin` es anterior a `inicio`
    public PeriodoTemporada {
        if (temporada == null || inicio == null || fin == null) {
            throw new RangoFechasInvalidoExcepcion("La temporada y sus fechas son obligatorias");
        }
        if (fin.isBefore(inicio)) {
            throw new RangoFechasInvalidoExcepcion("La temporada termina (" + fin + ") antes de empezar (" + inicio + ")");
        }
    }

    /// Indica si la fecha está dentro del periodo (ambos extremos incluidos).
    public boolean contiene(LocalDate fecha) {
        return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }

    /// Indica si este periodo comparte al menos un día con `otro`.
    public boolean solapa(PeriodoTemporada otro) {
        return !inicio.isAfter(otro.fin) && !otro.inicio.isAfter(fin);
    }
}
