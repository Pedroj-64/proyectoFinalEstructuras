package co.edu.uq.estructuras.sga.repository;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.estructuras.ArbolBinarioBusqueda;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;

import org.springframework.stereotype.Repository;

/// Catálogo de apartamentos guardado en un árbol binario de búsqueda ordenado por código.
@Repository
public class RepositorioApartamentosMemoria implements RepositorioApartamentos {

    /// Árbol de apartamentos ordenado por código.
    private final ArbolBinarioBusqueda<Apartamento> catalogo = new ArbolBinarioBusqueda<>();

    /// Inserta en el árbol; devuelve `false` si el código ya existía.
    @Override
    public boolean guardar(Apartamento apartamento) {
        return catalogo.insertar(apartamento);
    }

    /// Búsqueda por código en el árbol (sin distinguir mayúsculas).
    @Override
    public Apartamento buscarPorCodigo(String codigo) {
        String llave = codigo == null ? "" : codigo.trim().toUpperCase();
        return catalogo.buscarPor(apartamento -> llave.compareTo(apartamento.obtenerCodigo()));
    }

    /// Elimina del árbol por código.
    @Override
    public boolean eliminar(String codigo) {
        Apartamento apartamento = buscarPorCodigo(codigo);
        return apartamento != null && catalogo.eliminar(apartamento);
    }

    /// Recorrido inorden: apartamentos ordenados por código.
    @Override
    public ListaDoblementeEnlazada<Apartamento> buscarTodos() {
        return catalogo.enOrden();
    }

    /// Cantidad de apartamentos.
    @Override
    public int cantidad() {
        return catalogo.tamano();
    }

    /// Vacía el catálogo.
    @Override
    public void vaciar() {
        catalogo.vaciar();
    }
}
