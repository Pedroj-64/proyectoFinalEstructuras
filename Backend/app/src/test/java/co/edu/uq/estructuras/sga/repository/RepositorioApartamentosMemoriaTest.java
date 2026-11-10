package co.edu.uq.estructuras.sga.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.tarifas.TarifasTemporada;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas del repositorio de apartamentos respaldado por un árbol binario de búsqueda.
class RepositorioApartamentosMemoriaTest {

    /// Apartamento de apoyo con el código indicado.
    private static Apartamento apartamento(String codigo) {
        return new Apartamento(codigo, "Suite " + codigo, "A", 1, 1, 2, null,
                new TarifasTemporada(Dinero.de(100_000), Dinero.de(120_000), Dinero.de(150_000)));
    }

    @Test
    @DisplayName("guarda un apartamento y lo busca por código sin distinguir mayúsculas")
    void guardaYBusca() {
        RepositorioApartamentosMemoria repositorio = new RepositorioApartamentosMemoria();
        Apartamento apartamento = apartamento("A-101");

        assertTrue(repositorio.guardar(apartamento));
        assertEquals(1, repositorio.cantidad());
        assertSame(apartamento, repositorio.buscarPorCodigo("A-101"));
        assertSame(apartamento, repositorio.buscarPorCodigo(" a-101 "));
        assertNull(repositorio.buscarPorCodigo("A-999"));
    }

    @Test
    @DisplayName("no guarda dos apartamentos con el mismo código")
    void noRepiteCodigo() {
        RepositorioApartamentosMemoria repositorio = new RepositorioApartamentosMemoria();

        assertTrue(repositorio.guardar(apartamento("A-101")));
        assertFalse(repositorio.guardar(apartamento("a-101")));
        assertEquals(1, repositorio.cantidad());
    }

    @Test
    @DisplayName("elimina un apartamento existente y devuelve false si no existe")
    void elimina() {
        RepositorioApartamentosMemoria repositorio = new RepositorioApartamentosMemoria();
        repositorio.guardar(apartamento("A-101"));

        assertTrue(repositorio.eliminar("a-101"));
        assertFalse(repositorio.eliminar("A-101"));
        assertEquals(0, repositorio.cantidad());
    }

    @Test
    @DisplayName("buscarTodos entrega los apartamentos ordenados por código")
    void buscarTodosEnOrden() {
        RepositorioApartamentosMemoria repositorio = new RepositorioApartamentosMemoria();
        repositorio.guardar(apartamento("A-103"));
        repositorio.guardar(apartamento("A-101"));
        repositorio.guardar(apartamento("A-102"));

        var todos = repositorio.buscarTodos();

        assertEquals(3, todos.tamano());
        assertEquals("A-101", todos.obtener(0).obtenerCodigo());
        assertEquals("A-102", todos.obtener(1).obtenerCodigo());
        assertEquals("A-103", todos.obtener(2).obtenerCodigo());
    }

    @Test
    @DisplayName("vaciar deja el repositorio sin apartamentos")
    void vaciar() {
        RepositorioApartamentosMemoria repositorio = new RepositorioApartamentosMemoria();
        repositorio.guardar(apartamento("A-101"));
        repositorio.guardar(apartamento("A-102"));

        repositorio.vaciar();

        assertEquals(0, repositorio.cantidad());
        assertTrue(repositorio.buscarTodos().estaVacia());
    }
}
