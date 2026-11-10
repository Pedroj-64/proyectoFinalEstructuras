package co.edu.uq.estructuras.sga.repository;

import co.edu.uq.estructuras.sga.core.huespedes.Huesped;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;

/// Puerto para guardar huéspedes. El dominio depende de esta abstracción y no de la
/// implementación en memoria (principio de inversión de dependencias).
public interface RepositorioHuespedes {

    /// Guarda un huésped nuevo; devuelve `false` si el documento ya existe.
    boolean guardar(Huesped huesped);

    /// Huésped con ese documento, o `null`.
    Huesped buscarPorDocumento(String documento);

    /// Elimina el huésped; devuelve `false` si no existía.
    boolean eliminar(String documento);

    /// Todos los huéspedes ordenados por documento.
    ListaDoblementeEnlazada<Huesped> buscarTodos();

    /// Cantidad de huéspedes.
    int cantidad();

    /// Borra todos los huéspedes.
    void vaciar();
}
