package co.edu.uq.estructuras.sga.core.preparacion;

import java.time.Duration;
import java.time.LocalDateTime;

/// Preparación terminada, usada para el reporte de tiempo promedio de preparación.
///
/// @param codigoApartamento código del apartamento que se preparó
/// @param fechaInicio cuándo empezó la preparación
/// @param fechaFin cuándo terminó la preparación
public record RegistroPreparacion(String codigoApartamento, LocalDateTime fechaInicio, LocalDateTime fechaFin) {

    /// Minutos que tomó la preparación.
    ///
    /// @return la duración en minutos
    public long minutos() {
        return Duration.between(fechaInicio, fechaFin).toMinutes();
    }
}
