package co.edu.uq.estructuras.sga.core.apartamentos;

/// Estado operativo de un apartamento y las transiciones permitidas entre estados.
public enum EstadoApartamento {
    PREPARADO("Preparado"),
    OCUPADO("Ocupado"),
    PENDIENTE_PREPARACION("Pendiente de preparación"),
    EN_PREPARACION("En preparación"),
    FUERA_DE_SERVICIO("Fuera de servicio");

    /// Nombre en español para la interfaz.
    private final String nombreMostrado;

    EstadoApartamento(String nombreMostrado) {
        this.nombreMostrado = nombreMostrado;
    }

    /// Nombre en español para mostrar.
    public String nombreMostrado() {
        return nombreMostrado;
    }

    /// Ciclo de vida: PREPARADO → OCUPADO → PENDIENTE_PREPARACION → EN_PREPARACION → PREPARADO.
    /// Cualquier estado excepto OCUPADO puede pasar a FUERA_DE_SERVICIO, que vuelve pasando por preparación.
    public boolean puedePasarA(EstadoApartamento destino) {
        return switch (this) {
            case PREPARADO -> destino == OCUPADO || destino == FUERA_DE_SERVICIO;
            case OCUPADO -> destino == PENDIENTE_PREPARACION;
            case PENDIENTE_PREPARACION -> destino == EN_PREPARACION || destino == FUERA_DE_SERVICIO;
            case EN_PREPARACION -> destino == PREPARADO || destino == FUERA_DE_SERVICIO;
            case FUERA_DE_SERVICIO -> destino == PENDIENTE_PREPARACION || destino == PREPARADO;
        };
    }
}
