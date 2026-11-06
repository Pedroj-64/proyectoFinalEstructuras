package co.edu.uq.estructuras.sga.core.tarifas;

import java.time.LocalDate;

/// Decide qué porcentaje de lo pagado se devuelve cuando se cancela una reserva.
///
/// Es la interfaz del patrón Strategy: cada implementación define una regla de reembolso distinta
/// y se pueden intercambiar sin cambiar el código que las usa.
public interface PoliticaReembolso {

    /// Calcula qué porcentaje de lo pagado se devuelve al cancelar una estancia.
    ///
    /// @param fechaCancelacion día en que se cancela la reserva
    /// @param ingreso día de entrada de la estancia
    /// @return porcentaje de lo pagado que se devuelve, de 0 a 100
    int porcentajeReembolso(LocalDate fechaCancelacion, LocalDate ingreso);
}
