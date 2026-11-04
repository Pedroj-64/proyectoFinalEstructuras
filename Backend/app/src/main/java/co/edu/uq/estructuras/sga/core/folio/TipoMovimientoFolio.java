package co.edu.uq.estructuras.sga.core.folio;

import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;

/// Tipo de movimiento del folio. `signo` indica cómo afecta el saldo que debe el huésped.
public enum TipoMovimientoFolio {
    CARGO("Cargo", 1),
    PAGO("Pago", -1),
    ANULACION_CARGO("Anulación de cargo", -1),
    ANULACION_PAGO("Anulación de pago", 1),
    REEMBOLSO("Devolución", 1);

    /// Nombre en español para la interfaz.
    private final String nombreMostrado;
    /// +1 si aumenta lo que debe el huésped (cargo, devolución, anulación de pago), -1 si lo disminuye.
    private final int signo;

    TipoMovimientoFolio(String nombreMostrado, int signo) {
        this.nombreMostrado = nombreMostrado;
        this.signo = signo;
    }

    /// Nombre en español para mostrar.
    public String nombreMostrado() {
        return nombreMostrado;
    }

    /// +1 si aumenta lo que debe el huésped, -1 si lo disminuye.
    public int signo() {
        return signo;
    }

    /// Movimiento que anula a este. Los movimientos nunca se borran, se compensan.
    ///
    /// @throws ReglaNegocioExcepcion si el movimiento es `ANULACION_CARGO`, `ANULACION_PAGO` o `REEMBOLSO`, que no se pueden revertir
    public TipoMovimientoFolio contrario() {
        return switch (this) {
            case CARGO -> ANULACION_CARGO;
            case PAGO -> ANULACION_PAGO;
            case ANULACION_CARGO, ANULACION_PAGO, REEMBOLSO ->
                    throw new ReglaNegocioExcepcion("Un movimiento de " + nombreMostrado + " no se puede revertir");
        };
    }
}
