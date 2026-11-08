package co.edu.uq.estructuras.sga.core.tiempo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/// Fuente del "ahora". Los servicios no llaman `LocalDateTime.now()` directamente, así las
/// pruebas y la demostración pueden mover el reloj.
public interface ProveedorTiempo {

    /// Fecha y hora actuales según este reloj.
    LocalDateTime ahora();

    /// Fecha de hoy según este reloj.
    default LocalDate hoy() {
        return ahora().toLocalDate();
    }
}
