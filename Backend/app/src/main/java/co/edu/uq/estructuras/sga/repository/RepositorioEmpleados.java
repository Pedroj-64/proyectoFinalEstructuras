package co.edu.uq.estructuras.sga.repository;

import co.edu.uq.estructuras.sga.core.personal.Empleado;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;

/// Puerto para guardar empleados. El dominio depende de esta abstracción y no de la
/// implementación en memoria (principio de inversión de dependencias).
public interface RepositorioEmpleados {

    /// Guarda un empleado nuevo; devuelve `false` si ya existe esa identificación.
    boolean guardar(Empleado empleado);

    /// Empleado con esa identificación, o `null`.
    Empleado buscarPorId(String id);

    /// Elimina el empleado; devuelve `false` si no existía.
    boolean eliminar(String id);

    /// Todos los empleados ordenados por identificación.
    ListaDoblementeEnlazada<Empleado> buscarTodos();

    /// Cantidad de empleados.
    int cantidad();

    /// Borra todos los empleados.
    void vaciar();
}
