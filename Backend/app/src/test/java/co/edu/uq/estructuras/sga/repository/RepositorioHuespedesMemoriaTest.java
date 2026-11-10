package co.edu.uq.estructuras.sga.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.estructuras.sga.core.huespedes.Huesped;
import co.edu.uq.estructuras.sga.core.huespedes.PerfilHuesped;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas del repositorio de huéspedes respaldado por un árbol binario de búsqueda.
class RepositorioHuespedesMemoriaTest {

    /// Huésped de apoyo con el documento indicado.
    private static Huesped huesped(String documento) {
        return new Huesped(new PerfilHuesped(documento, "Huésped " + documento, LocalDate.of(1990, 1, 1),
                "300", documento + "@uq.edu.co", null));
    }

    @Test
    @DisplayName("guarda un huésped y lo busca por documento")
    void guardaYBusca() {
        RepositorioHuespedesMemoria repositorio = new RepositorioHuespedesMemoria();
        Huesped huesped = huesped("123");

        assertTrue(repositorio.guardar(huesped));
        assertEquals(1, repositorio.cantidad());
        assertSame(huesped, repositorio.buscarPorDocumento("123"));
        assertSame(huesped, repositorio.buscarPorDocumento(" 123 "));
        assertNull(repositorio.buscarPorDocumento("999"));
    }

    @Test
    @DisplayName("no guarda dos huéspedes con el mismo documento")
    void noRepiteDocumento() {
        RepositorioHuespedesMemoria repositorio = new RepositorioHuespedesMemoria();

        assertTrue(repositorio.guardar(huesped("123")));
        assertFalse(repositorio.guardar(huesped(" 123 ")));
        assertEquals(1, repositorio.cantidad());
    }

    @Test
    @DisplayName("elimina un huésped existente y devuelve false si no existe")
    void elimina() {
        RepositorioHuespedesMemoria repositorio = new RepositorioHuespedesMemoria();
        repositorio.guardar(huesped("123"));

        assertTrue(repositorio.eliminar("123"));
        assertFalse(repositorio.eliminar("123"));
        assertEquals(0, repositorio.cantidad());
    }

    @Test
    @DisplayName("buscarTodos entrega los huéspedes ordenados por documento")
    void buscarTodosEnOrden() {
        RepositorioHuespedesMemoria repositorio = new RepositorioHuespedesMemoria();
        repositorio.guardar(huesped("103"));
        repositorio.guardar(huesped("101"));
        repositorio.guardar(huesped("102"));

        var todos = repositorio.buscarTodos();

        assertEquals(3, todos.tamano());
        assertEquals("101", todos.obtener(0).obtenerDocumento());
        assertEquals("102", todos.obtener(1).obtenerDocumento());
        assertEquals("103", todos.obtener(2).obtenerDocumento());
    }

    @Test
    @DisplayName("vaciar deja el repositorio sin huéspedes")
    void vaciar() {
        RepositorioHuespedesMemoria repositorio = new RepositorioHuespedesMemoria();
        repositorio.guardar(huesped("123"));
        repositorio.guardar(huesped("456"));

        repositorio.vaciar();

        assertEquals(0, repositorio.cantidad());
        assertTrue(repositorio.buscarTodos().estaVacia());
    }
}
