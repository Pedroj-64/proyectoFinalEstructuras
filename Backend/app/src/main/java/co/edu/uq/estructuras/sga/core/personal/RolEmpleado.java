package co.edu.uq.estructuras.sga.core.personal;

/// Rol de un empleado dentro del alojamiento.
public enum RolEmpleado {
    RECEPCIONISTA("Recepcionista"),
    ASEO("Personal de preparación"),
    ADMINISTRADOR("Administrador");

    /// Nombre en español para la interfaz.
    private final String nombreMostrado;

    RolEmpleado(String nombreMostrado) {
        this.nombreMostrado = nombreMostrado;
    }

    /// Nombre en español para mostrar.
    public String nombreMostrado() {
        return nombreMostrado;
    }
}
