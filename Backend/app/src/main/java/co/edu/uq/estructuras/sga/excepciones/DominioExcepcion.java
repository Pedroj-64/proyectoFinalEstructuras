package co.edu.uq.estructuras.sga.excepciones;

/// Raíz de toda violación de una regla de negocio.
///
/// Es `sealed`: el compilador conoce la lista completa de errores del dominio, así la capa web
/// puede traducirlos con un `switch` exhaustivo y sin rama `default`.
public abstract sealed class DominioExcepcion extends RuntimeException
        permits NoEncontradoExcepcion, ReservaSolapadaExcepcion, CapacidadExcedidaExcepcion,
        RangoFechasInvalidoExcepcion, TransicionEstadoInvalidaExcepcion, ReglaNegocioExcepcion {

    /// Solo las subclases permitidas pueden construirla.
    ///
    /// @param mensaje mensaje en español que verá el usuario
    protected DominioExcepcion(String mensaje) {
        super(mensaje);
    }
}
