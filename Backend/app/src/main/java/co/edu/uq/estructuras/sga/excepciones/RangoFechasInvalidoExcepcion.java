package co.edu.uq.estructuras.sga.excepciones;

/// Una estancia o un periodo tiene fechas inconsistentes (salida antes de la entrada, fechas pasadas, etc.).
public final class RangoFechasInvalidoExcepcion extends DominioExcepcion {

    /// @param message qué está mal en las fechas
    public RangoFechasInvalidoExcepcion(String mensaje) {
        super(mensaje);
    }
}
