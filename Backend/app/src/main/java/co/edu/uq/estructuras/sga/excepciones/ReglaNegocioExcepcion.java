package co.edu.uq.estructuras.sga.excepciones;

/// Cualquier otra violación de una regla de negocio (saldo pendiente en el check-out, código repetido...).
public final class ReglaNegocioExcepcion extends DominioExcepcion {

    /// @param mensaje explicación en español de la regla violada
    public ReglaNegocioExcepcion(String mensaje) {
        super(mensaje);
    }
}
