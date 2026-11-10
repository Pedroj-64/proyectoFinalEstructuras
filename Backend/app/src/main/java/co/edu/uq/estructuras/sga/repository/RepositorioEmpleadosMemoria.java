package co.edu.uq.estructuras.sga.repository;

import co.edu.uq.estructuras.sga.core.personal.Empleado;
import co.edu.uq.estructuras.sga.estructuras.ArbolBinarioBusqueda;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;

import org.springframework.stereotype.Repository;

/// Empleados guardados en un árbol binario de búsqueda ordenado por identificación.
@Repository
public class RepositorioEmpleadosMemoria implements RepositorioEmpleados {

    /// Árbol de empleados ordenado por identificación.
    private final ArbolBinarioBusqueda<Empleado> empleados = new ArbolBinarioBusqueda<>();

    /// Inserta en el árbol; devuelve `false` si la identificación ya existía.
    @Override
    public boolean guardar(Empleado empleado) {
        return empleados.insertar(empleado);
    }

    /// Búsqueda por identificación en el árbol.
    @Override
    public Empleado buscarPorId(String id) {
        String llave = id == null ? "" : id.trim();
        return empleados.buscarPor(empleado -> llave.compareTo(empleado.obtenerId()));
    }

    /// Elimina del árbol por identificación.
    @Override
    public boolean eliminar(String id) {
        Empleado empleado = buscarPorId(id);
        return empleado != null && empleados.eliminar(empleado);
    }

    /// Recorrido inorden: empleados ordenados por identificación.
    @Override
    public ListaDoblementeEnlazada<Empleado> buscarTodos() {
        return empleados.enOrden();
    }

    /// Cantidad de empleados.
    @Override
    public int cantidad() {
        return empleados.tamano();
    }

    /// Vacía el repositorio.
    @Override
    public void vaciar() {
        empleados.vaciar();
    }
}
