package co.edu.uq.estructuras.sga.core.canal;

/// Puerto por el que el canal externo crea reservas sin conocer el módulo de reservas.
/// Lo implementa el servicio de reservas.
public interface PuertoReservasExternas {

    /// Crea la reserva de la solicitud y devuelve su id. Lanza una
    /// [co.edu.uq.estructuras.sga.excepciones.DominioExcepcion] si no se puede (por ejemplo, solapamiento).
    String reservar(SolicitudReservaExterna solicitud);
}
