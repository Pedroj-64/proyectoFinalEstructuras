package co.edu.uq.estructuras.sga.excepciones;


/// Dos reservas activas del mismo apartamento compartirían al menos una noche.
public final class ReservaSolapadaExcepcion extends DominioExcepcion {

    /// Reserva activa con la que se choca.
    private final String idReservaEnConflicto;

    /// Arma el mensaje antes de llamar a `super` (cuerpo de constructor flexible, JEP 513).
    ///
    /// @param codigoApartamento apartamento pedido
    /// @param pedida estancia pedida
    /// @param idReservaEnConflicto reserva con la que choca
    public ReservaSolapadaExcepcion(String codigoApartamento, String pedida, String idReservaEnConflicto) {
        // Java 25 (JEP 513): se permiten sentencias antes de super(...) mientras no usen `this`.
        String mensaje = "El apartamento %s ya tiene la reserva %s que comparte noches con %s"
                .formatted(codigoApartamento, idReservaEnConflicto, pedida);
        super(mensaje);
        this.idReservaEnConflicto = idReservaEnConflicto;
    }

    /// Id de la reserva activa con la que choca la estancia pedida.
    public String obtenerIdReservaEnConflicto() {
        return idReservaEnConflicto;
    }
}
