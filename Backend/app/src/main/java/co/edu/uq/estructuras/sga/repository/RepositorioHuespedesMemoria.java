package co.edu.uq.estructuras.sga.repository;

import co.edu.uq.estructuras.sga.core.huespedes.Huesped;
import co.edu.uq.estructuras.sga.estructuras.ArbolBinarioBusqueda;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;

import org.springframework.stereotype.Repository;

/// Huéspedes guardados en un árbol binario de búsqueda ordenado por documento.
@Repository
public class RepositorioHuespedesMemoria implements RepositorioHuespedes {

    /// Árbol de huéspedes ordenado por documento.
    private final ArbolBinarioBusqueda<Huesped> huespedes = new ArbolBinarioBusqueda<>();

    /// Inserta en el árbol; devuelve `false` si el documento ya existía.
    @Override
    public boolean guardar(Huesped huesped) {
        return huespedes.insertar(huesped);
    }

    /// Búsqueda por documento en el árbol.
    @Override
    public Huesped buscarPorDocumento(String documento) {
        String llave = documento == null ? "" : documento.trim();
        return huespedes.buscarPor(huesped -> llave.compareTo(huesped.obtenerDocumento()));
    }

    /// Elimina del árbol por documento.
    @Override
    public boolean eliminar(String documento) {
        Huesped huesped = buscarPorDocumento(documento);
        return huesped != null && huespedes.eliminar(huesped);
    }

    /// Recorrido inorden: huéspedes ordenados por documento.
    @Override
    public ListaDoblementeEnlazada<Huesped> buscarTodos() {
        return huespedes.enOrden();
    }

    /// Cantidad de huéspedes.
    @Override
    public int cantidad() {
        return huespedes.tamano();
    }

    /// Vacía el repositorio.
    @Override
    public void vaciar() {
        huespedes.vaciar();
    }
}
