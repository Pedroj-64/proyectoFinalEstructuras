package co.edu.uq.estructuras.sga.excepciones;

/// Una reserva o un apartamento intentó pasar a un estado que su ciclo de vida no permite.
public final class TransicionEstadoInvalidaExcepcion extends DominioExcepcion {

    /// @param entidad entidad afectada
    /// @param desde estado actual
    /// @param hasta estado al que se intentó pasar
    public TransicionEstadoInvalidaExcepcion(String entidad, String desde, String hasta) {
        super("%s no puede pasar de '%s' a '%s'".formatted(entidad, desde, hasta));
    }
}
