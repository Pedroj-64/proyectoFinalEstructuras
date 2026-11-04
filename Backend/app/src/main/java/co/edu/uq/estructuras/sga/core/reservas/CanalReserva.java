package co.edu.uq.estructuras.sga.core.reservas;

/// Canal por el que llega una reserva.
public enum CanalReserva {
    PORTAL("Portal"),
    DIRECTO("Directa"),
    EXTERNO("Externa");

    /// Nombre en español para la interfaz.
    private final String nombreMostrado;

    CanalReserva(String nombreMostrado) {
        this.nombreMostrado = nombreMostrado;
    }

    /// Nombre en español para mostrar.
    public String nombreMostrado() {
        return nombreMostrado;
    }
}
