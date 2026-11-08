package co.edu.uq.estructuras.sga.core.tarifas;

import co.edu.uq.estructuras.sga.core.reservas.CanalReserva;
import co.edu.uq.estructuras.sga.core.reservas.EstadoReserva;

/// Reglas que cambian de un canal de reserva a otro (patrón Strategy).
///
/// Cada canal tiene su propia implementación, y quien usa la política solo conoce esta interfaz.
/// Agregar un canal es agregar una implementación (más su valor en [CanalReserva] y su caso en
/// [PoliticasCanal]) sin modificar el código que usa las políticas (principio abierto/cerrado).
public interface PoliticaCanal {

    /// Canal al que aplica esta política.
    ///
    /// @return canal de reserva de esta política
    CanalReserva canal();

    /// Estado con el que nace una reserva de este canal.
    ///
    /// @return `PENDIENTE` o `CONFIRMADA`, según el canal
    EstadoReserva estadoInicial();

    /// Indica si una reserva de este canal vence cuando el anticipo no se paga a tiempo.
    ///
    /// @return `true` si vence sin anticipo; `false` si no vence
    boolean venceSinAnticipo();
}
