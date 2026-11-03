package co.edu.uq.estructuras.sga.excepciones;

/// El número de ocupantes supera la capacidad disponible.
public final class CapacidadExcedidaExcepcion extends DominioExcepcion {

    /// @param destino qué se intentó llenar (por ejemplo, "El apartamento A-101")
    /// @param pedida personas pedidas
    /// @param capacidad capacidad máxima
    public CapacidadExcedidaExcepcion(String destino, int pedida, int capacidad) {
        super("%s admite máximo %d personas y se solicitaron %d".formatted(destino, capacidad, pedida));
    }
}
