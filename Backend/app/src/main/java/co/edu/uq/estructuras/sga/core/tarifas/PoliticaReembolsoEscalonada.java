package co.edu.uq.estructuras.sga.core.tarifas;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/// Política de reembolso escalonada según los días que faltan para la entrada:
/// 7 días o más antes de la entrada → 100 %, de 2 a 6 días → 50 %, menos de 2 días → 0 %
/// (incluye cancelar el mismo día de la entrada o después).
public final class PoliticaReembolsoEscalonada implements PoliticaReembolso {

    /// Calcula el porcentaje a devolver según cuántos días faltan desde `fechaCancelacion` hasta `ingreso`.
    ///
    /// @param fechaCancelacion día en que se cancela la reserva
    /// @param ingreso día de entrada de la estancia
    /// @return 100 si faltan 7 días o más, 50 si faltan de 2 a 6 días y 0 en cualquier otro caso
    @Override
    public int porcentajeReembolso(LocalDate fechaCancelacion, LocalDate ingreso) {
        long diasAntes = ChronoUnit.DAYS.between(fechaCancelacion, ingreso);
        if (diasAntes >= 7) {
            return 100;
        }
        return diasAntes >= 2 ? 50 : 0;
    }
}
