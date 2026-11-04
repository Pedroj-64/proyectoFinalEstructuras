package co.edu.uq.estructuras.sga.core.personal;

import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;

/// Miembro del personal. Orden natural: por número de identificación.
public class Empleado implements Comparable<Empleado> {

    private final String id;
    private String nombreCompleto;
    private RolEmpleado rol;
    private String correo;
    private String telefono;

    /// Crea el empleado validando que id, nombre y rol estén presentes.
    public Empleado(String id, String nombreCompleto, RolEmpleado rol, String correo, String telefono) {
        if (id == null || id.isBlank()) {
            throw new ReglaNegocioExcepcion("La identificación del empleado es obligatoria");
        }
        this.id = id.trim();
        actualizar(nombreCompleto, rol, correo, telefono);
    }

    /// Actualiza los datos del empleado.
    public void actualizar(String nombreCompleto, RolEmpleado rol, String correo, String telefono) {
        if (nombreCompleto == null || nombreCompleto.isBlank() || rol == null) {
            throw new ReglaNegocioExcepcion("Nombre y rol del empleado son obligatorios");
        }
        this.nombreCompleto = nombreCompleto.trim();
        this.rol = rol;
        this.correo = correo;
        this.telefono = telefono;
    }

    /// Identificación (llave del árbol de empleados).
    public String obtenerId() {
        return id;
    }

    /// Nombre completo.
    public String obtenerNombreCompleto() {
        return nombreCompleto;
    }

    /// Rol (recepcionista o administrador).
    public RolEmpleado obtenerRol() {
        return rol;
    }

    /// Correo electrónico.
    public String obtenerCorreo() {
        return correo;
    }

    /// Teléfono.
    public String obtenerTelefono() {
        return telefono;
    }

    /// Orden natural: por identificación.
    @Override
    public int compareTo(Empleado otro) {
        return id.compareTo(otro.id);
    }

    /// Dos empleados son iguales si tienen la misma identificación.
    @Override
    public boolean equals(Object otro) {
        return otro instanceof Empleado empleado && id.equals(empleado.id);
    }

    /// Consistente con [#equals(Object)]: usa la identificación.
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
