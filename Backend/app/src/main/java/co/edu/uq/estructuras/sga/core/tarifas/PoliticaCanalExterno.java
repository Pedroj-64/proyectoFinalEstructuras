package co.edu.uq.estructuras.sga.core.tarifas;

import co.edu.uq.estructuras.sga.core.reservas.CanalReserva;
import co.edu.uq.estructuras.sga.core.reservas.EstadoReserva;

/// Canal externo: la plataforma ya le cobró al huésped, así que la reserva nace confirmada y no vence.
public final class PoliticaCanalExterno implements PoliticaCanal {

    /// Canal externo.
    ///
    /// @return `CanalReserva.EXTERNO`
    @Override
    public CanalReserva canal() {
        return CanalReserva.EXTERNO;
    }

    /// La reserva externa nace confirmada.
    ///
    /// @return `EstadoReserva.CONFIRMADA`
    @Override
    public EstadoReserva estadoInicial() {
        return EstadoReserva.CONFIRMADA;
    }

    /// La reserva externa no vence por falta de anticipo.
    ///
    /// @return siempre `false`
    @Override
    public boolean venceSinAnticipo() {
        return false;
    }
}
