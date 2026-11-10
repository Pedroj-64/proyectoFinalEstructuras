package co.edu.uq.estructuras.sga.repository;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;


/// Puerto para guardar apartamentos. El dominio depende de esta abstracción y no de la
/// implementación en memoria (principio de inversión de dependencias).
public interface RepositorioApartamentos {

    /// Guarda un apartamento nuevo; devuelve `false` si el código ya existe.
    boolean guardar(Apartamento apartamento);

    /// Apartamento con ese código, o `null`.
    Apartamento buscarPorCodigo(String codigo);

    /// Elimina el apartamento; devuelve `false` si no existía.
    boolean eliminar(String codigo);

    /// Todos los apartamentos ordenados por código.
    ListaDoblementeEnlazada<Apartamento> buscarTodos();

    /// Cantidad de apartamentos.
    int cantidad();

    /// Borra todos los apartamentos.
    void vaciar();
}

