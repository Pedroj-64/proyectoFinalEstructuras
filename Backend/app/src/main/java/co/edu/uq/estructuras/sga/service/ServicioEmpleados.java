package co.edu.uq.estructuras.sga.service;

import co.edu.uq.estructuras.sga.core.personal.Empleado;
import co.edu.uq.estructuras.sga.core.personal.RolEmpleado;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.NoEncontradoExcepcion;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.repository.RepositorioEmpleados;

import org.springframework.stereotype.Service;

/// Gestión del personal (administrador).
@Service
public class ServicioEmpleados {

    private final RepositorioEmpleados empleados;

    /// Recibe por inyección el repositorio de empleados.
    public ServicioEmpleados(RepositorioEmpleados empleados) {
        this.empleados = empleados;
    }

    /// Todos los empleados en orden de identificación.
    public ListaDoblementeEnlazada<Empleado> buscarTodos() {
        return empleados.buscarTodos();
    }

    /// Busca un empleado o lanza [NoEncontradoExcepcion].
    public Empleado buscar(String id) {
        Empleado empleado = empleados.buscarPorId(id);
        if (empleado == null) {
            throw new NoEncontradoExcepcion("El empleado", id);
        }
        return empleado;
    }

    /// Registra un empleado; la identificación no se puede repetir.
    public Empleado crear(String id, String nombreCompleto, RolEmpleado rol, String correo, String telefono) {
        Empleado empleado = new Empleado(id, nombreCompleto, rol, correo, telefono);
        if (!empleados.guardar(empleado)) {
            throw new ReglaNegocioExcepcion("Ya existe un empleado con la identificación " + empleado.obtenerId());
        }
        return empleado;
    }

    /// Actualiza los datos de un empleado.
    public Empleado actualizar(String id, String nombreCompleto, RolEmpleado rol, String correo, String telefono) {
        Empleado empleado = buscar(id);
        empleado.actualizar(nombreCompleto, rol, correo, telefono);
        return empleado;
    }

    /// Elimina un empleado.
    public void eliminar(String id) {
        if (!empleados.eliminar(id)) {
            throw new NoEncontradoExcepcion("El empleado", id);
        }
    }

    /// Recepcionistas que participan en la rotación de turnos.
    public ListaDoblementeEnlazada<Empleado> recepcionistas() {
        return empleados.buscarTodos().filtrar(empleado -> empleado.obtenerRol() == RolEmpleado.RECEPCIONISTA);
    }
}
