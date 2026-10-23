package co.edu.uq.estructuras.sga.excepciones;

public class EstructuraVaciaExcepcion extends RuntimeException {

    /// @param nombreEstructura nombre de la estructura vacía (Stack, Queue...)
    public EstructuraVaciaExcepcion(String nombreEstructura) {
        super("La estructura '" + nombreEstructura + "' está vacía");
    }
}
