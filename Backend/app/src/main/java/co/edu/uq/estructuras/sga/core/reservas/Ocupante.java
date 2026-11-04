package co.edu.uq.estructuras.sga.core.reservas;

import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;

import java.time.LocalDate;
import java.time.Period;

/// Persona que se hospeda en una reserva. Los menores de `EDAD_LIMITE_GRATIS` (6) años no pagan.
public record Ocupante(String nombreCompleto, LocalDate fechaNacimiento) {

    public static final int EDAD_LIMITE_GRATIS = 6;

    public Ocupante {
        if (nombreCompleto == null || nombreCompleto.isBlank() || fechaNacimiento == null) {
            throw new ReglaNegocioExcepcion("Cada ocupante necesita nombre y fecha de nacimiento");
        }
    }

    /// Edad cumplida en la fecha indicada.
    public int edadEn(LocalDate fecha) {
        return Period.between(fechaNacimiento, fecha).getYears();
    }

    /// Indica si paga esa noche (los menores de 6 años no pagan).
    public boolean pagaEn(LocalDate noche) {
        return edadEn(noche) >= EDAD_LIMITE_GRATIS;
    }
}
