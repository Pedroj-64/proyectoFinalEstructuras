package co.edu.uq.estructuras.sga.core.folio;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import java.time.LocalDateTime;

/// Movimiento inmutable del folio. `anulaMovimiento` apunta al movimiento que compensa, si lo hay.
public record MovimientoFolio(int numero, TipoMovimientoFolio tipo, Dinero monto, String descripcion,
                         LocalDateTime fechaCreacion, Integer anulaMovimiento) {

    /// Valor con signo según cómo afecta el saldo del huésped.
    public Dinero montoConSigno() {
        return monto.veces(tipo.signo());
    }
}
