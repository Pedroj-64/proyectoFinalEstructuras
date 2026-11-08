package co.edu.uq.estructuras.sga.core.tarifas;

import co.edu.uq.estructuras.sga.core.reservas.CanalReserva;
import co.edu.uq.estructuras.sga.core.reservas.EstadoReserva;

/// Canal directo: la registra recepción (teléfono o presencial); la reserva nace confirmada y no vence.
public final class PoliticaCanalDirecto implements PoliticaCanal {

    /// Canal directo.
    ///
    /// @return `CanalReserva.DIRECTO`
    @Override
    public CanalReserva canal() {
        return CanalReserva.DIRECTO;
    }

    /// La reserva directa nace confirmada.
    ///
    /// @return `EstadoReserva.CONFIRMADA`
    @Override
    public EstadoReserva estadoInicial() {
        return EstadoReserva.CONFIRMADA;
    }

    /// La reserva directa no vence por falta de anticipo.
    ///
    /// @return siempre `false`
    @Override
    public boolean venceSinAnticipo() {
        return false;
    }
}
