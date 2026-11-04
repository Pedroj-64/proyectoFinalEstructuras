package co.edu.uq.estructuras.sga.core.reservas;

/// Ciclo de vida de la reserva: PENDIENTE → CONFIRMADA → EN_CURSO → FINALIZADA,
/// con los finales anticipados CANCELADA (desde PENDIENTE o CONFIRMADA) y NO_PRESENTADO (desde CONFIRMADA).
public enum EstadoReserva {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    EN_CURSO("En curso"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada"),
    NO_PRESENTADO("No presentado");

    /// Nombre legible para mostrar en la interfaz.
    private final String nombreMostrado;

    EstadoReserva(String nombreMostrado) {
        this.nombreMostrado = nombreMostrado;
    }

    /// Devuelve el nombre legible para mostrar en la interfaz.
    public String nombreMostrado() {
        return nombreMostrado;
    }

    /// Indica si la reserva está activa: devuelve `true` en PENDIENTE, CONFIRMADA y EN_CURSO,
    /// y `false` en FINALIZADA, CANCELADA y NO_PRESENTADO.
    public boolean estaActiva() {
        return this == PENDIENTE || this == CONFIRMADA || this == EN_CURSO;
    }

    /// Indica si el ciclo de vida permite pasar al estado `destino`.
    public boolean puedePasarA(EstadoReserva destino) {
        return switch (this) {
            case PENDIENTE -> destino == CONFIRMADA || destino == CANCELADA;
            case CONFIRMADA -> destino == EN_CURSO || destino == CANCELADA || destino == NO_PRESENTADO;
            case EN_CURSO -> destino == FINALIZADA;
            case FINALIZADA, CANCELADA, NO_PRESENTADO -> false;
        };
    }
}
