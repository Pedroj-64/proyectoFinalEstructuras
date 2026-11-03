package co.edu.uq.estructuras.sga.excepciones;

/// Una entidad pedida (apartamento, huésped, reserva...) no existe.
public final class NoEncontradoExcepcion extends DominioExcepcion {

    /// @param entidad qué se buscó (por ejemplo, "El huésped")
    /// @param llave llave usada en la búsqueda
    public NoEncontradoExcepcion(String entidad, String llave) {
        super(entidad + " '" + llave + "' no existe");
    }
}
