package co.edu.uq.estructuras.sga.core.tarifas;

import co.edu.uq.estructuras.sga.core.reservas.CanalReserva;

/// Fábrica que entrega la estrategia de cada canal (patrón Factory).
///
/// Quien necesita una [PoliticaCanal] la pide por su canal y no crea ni elige la implementación concreta.
/// Las políticas no guardan estado, así que se crea una sola instancia de cada una y siempre se devuelve esa.
public final class PoliticasCanal {

    /// Estrategia del portal: nace pendiente y vence sin anticipo.
    private static final PoliticaCanal PORTAL = new PoliticaCanalPortal();
    /// Estrategia directa: nace confirmada.
    private static final PoliticaCanal DIRECTO = new PoliticaCanalDirecto();
    /// Estrategia externa: nace confirmada.
    private static final PoliticaCanal EXTERNO = new PoliticaCanalExterno();

    /// Clase de utilidades (fábrica): no se instancia.
    private PoliticasCanal() {
    }

    /// Estrategia correspondiente al canal.
    ///
    /// @param canal canal por el que llega la reserva
    /// @return la única instancia de la política de ese canal
    public static PoliticaCanal paraCanal(CanalReserva canal) {
        return switch (canal) {
            case PORTAL -> PORTAL;
            case DIRECTO -> DIRECTO;
            case EXTERNO -> EXTERNO;
        };
    }
}
