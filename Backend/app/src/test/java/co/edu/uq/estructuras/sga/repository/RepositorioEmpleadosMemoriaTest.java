package co.edu.uq.estructuras.sga.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.estructuras.sga.core.personal.Empleado;
import co.edu.uq.estructuras.sga.core.personal.RolEmpleado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas del repositorio de empleados respaldado por un árbol binario de búsqueda.
class RepositorioEmpleadosMemoriaTest {

    /// Empleado de apoyo con la identificación indicada.
    private static Empleado empleado(String id) {
        return new Empleado(id, "Empleado " + id, RolEmpleado.RECEPCIONISTA, id + "@uq.edu.co", "300");
    }

    @Test
    @DisplayName("guarda un empleado y lo busca por identificación")
    void guardaYBusca() {
        RepositorioEmpleadosMemoria repositorio = new RepositorioEmpleadosMemoria();
        Empleado empleado = empleado("101");

        assertTrue(repositorio.guardar(empleado));
        assertEquals(1, repositorio.cantidad());
        assertSame(empleado, repositorio.buscarPorId("101"));
        assertSame(empleado, repositorio.buscarPorId(" 101 "));
        assertNull(repositorio.buscarPorId("999"));
    }

    @Test
    @DisplayName("no guarda dos empleados con la misma identificación")
    void noRepiteIdentificacion() {
        RepositorioEmpleadosMemoria repositorio = new RepositorioEmpleadosMemoria();

        assertTrue(repositorio.guardar(empleado("101")));
        assertFalse(repositorio.guardar(empleado(" 101 ")));
        assertEquals(1, repositorio.cantidad());
    }

    @Test
    @DisplayName("elimina un empleado existente y devuelve false si no existe")
    void elimina() {
        RepositorioEmpleadosMemoria repositorio = new RepositorioEmpleadosMemoria();
        repositorio.guardar(empleado("101"));

        assertTrue(repositorio.eliminar("101"));
        assertFalse(repositorio.eliminar("101"));
        assertEquals(0, repositorio.cantidad());
    }

    @Test
    @DisplayName("buscarTodos entrega los empleados ordenados por identificación")
    void buscarTodosEnOrden() {
        RepositorioEmpleadosMemoria repositorio = new RepositorioEmpleadosMemoria();
        repositorio.guardar(empleado("103"));
        repositorio.guardar(empleado("101"));
        repositorio.guardar(empleado("102"));

        var todos = repositorio.buscarTodos();

        assertEquals(3, todos.tamano());
        assertEquals("101", todos.obtener(0).obtenerId());
        assertEquals("102", todos.obtener(1).obtenerId());
        assertEquals("103", todos.obtener(2).obtenerId());
    }

    @Test
    @DisplayName("vaciar deja el repositorio sin empleados")
    void vaciar() {
        RepositorioEmpleadosMemoria repositorio = new RepositorioEmpleadosMemoria();
        repositorio.guardar(empleado("101"));
        repositorio.guardar(empleado("102"));

        repositorio.vaciar();

        assertEquals(0, repositorio.cantidad());
        assertTrue(repositorio.buscarTodos().estaVacia());
    }
}
