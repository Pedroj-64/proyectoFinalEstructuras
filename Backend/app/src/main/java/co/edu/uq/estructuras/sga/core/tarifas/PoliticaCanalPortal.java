package co.edu.uq.estructuras.sga.core.tarifas;

import co.edu.uq.estructuras.sga.core.reservas.CanalReserva;
import co.edu.uq.estructuras.sga.core.reservas.EstadoReserva;

/// Canal portal: la reserva nace PENDIENTE y vence si el huésped no paga el anticipo a tiempo.
/// El porcentaje del anticipo y el plazo para pagarlo no se definen en esta clase.
public final class PoliticaCanalPortal implements PoliticaCanal {

    /// Canal portal.
    ///
    /// @return `CanalReserva.PORTAL`
    @Override
    public CanalReserva canal() {
        return CanalReserva.PORTAL;
    }

    /// La reserva del portal nace pendiente.
    ///
    /// @return `EstadoReserva.PENDIENTE`
    @Override
    public EstadoReserva estadoInicial() {
        return EstadoReserva.PENDIENTE;
    }

    /// La reserva del portal vence si no se paga el anticipo a tiempo.
    ///
    /// @return siempre `true`
    @Override
    public boolean venceSinAnticipo() {
        return true;
    }
}
