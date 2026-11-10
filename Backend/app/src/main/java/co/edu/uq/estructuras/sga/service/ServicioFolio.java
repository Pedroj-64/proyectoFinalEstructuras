package co.edu.uq.estructuras.sga.service;

import co.edu.uq.estructuras.sga.core.folio.Folio;
import co.edu.uq.estructuras.sga.core.folio.MovimientoFolio;
import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.reservas.EstadoReserva;
import co.edu.uq.estructuras.sga.core.reservas.Reserva;
import co.edu.uq.estructuras.sga.excepciones.NoEncontradoExcepcion;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.service.tiempo.ProveedorTiempo;

import org.springframework.stereotype.Service;

/// Cargos, pagos y deshacer sobre el folio de una reserva, usados por recepción y por el huésped.
/// La fecha de cada movimiento la pone el reloj del sistema ([ProveedorTiempo]).
@Service
public class ServicioFolio {

    private final ServicioReservas reservas;
    private final ProveedorTiempo tiempo;

    /// Recibe por inyección el servicio de reservas y el reloj.
    ///
    /// @param reservas servicio para buscar reservas
    /// @param tiempo reloj del sistema
    public ServicioFolio(ServicioReservas reservas, ProveedorTiempo tiempo) {
        this.reservas = reservas;
        this.tiempo = tiempo;
    }

    /// Caso de uso: consultar el folio (la cuenta) de una reserva.
    ///
    /// @param idReserva id de la reserva
    /// @return el folio de la reserva
    /// @throws NoEncontradoExcepcion si la reserva no existe
    public Folio folioDe(String idReserva) {
        return reservas.buscar(idReserva).obtenerFolio();
    }

    /// Caso de uso: recepción registra un cargo adicional (minibar, lavandería...). El cargo se puede deshacer.
    ///
    /// @param idReserva id de la reserva
    /// @param monto valor del cargo
    /// @param descripcion concepto del cargo; si está vacía se usa el nombre del tipo de movimiento
    /// @return el movimiento registrado
    /// @throws NoEncontradoExcepcion si la reserva no existe
    /// @throws ReglaNegocioExcepcion si el folio está cerrado o si `monto` no es mayor que cero
    public MovimientoFolio agregarCargo(String idReserva, Dinero monto, String descripcion) {
        Reserva reserva = reservas.buscar(idReserva);
        exigirAbierto(reserva);
        return reserva.obtenerFolio().cargo(monto, descripcion, tiempo.ahora());
    }

    /// Caso de uso: registrar un pago. Si la reserva está pendiente y con este pago queda cubierto el
    /// anticipo del 30 %, la reserva pasa a confirmada. El pago se puede deshacer.
    ///
    /// @param idReserva id de la reserva
    /// @param monto valor pagado
    /// @param descripcion concepto del pago; si está vacía se usa el nombre del tipo de movimiento
    /// @return el movimiento registrado
    /// @throws NoEncontradoExcepcion si la reserva no existe
    /// @throws ReglaNegocioExcepcion si el folio está cerrado o si `monto` no es mayor que cero
    public MovimientoFolio agregarPago(String idReserva, Dinero monto, String descripcion) {
        Reserva reserva = reservas.buscar(idReserva);
        exigirAbierto(reserva);
        MovimientoFolio movimiento = reserva.obtenerFolio().pagar(monto, descripcion, tiempo.ahora());
        reserva.confirmarSiAnticipoCubierto();
        return movimiento;
    }

    /// Caso de uso: deshacer la última operación de recepción (cargo o pago). No borra nada: registra el
    /// movimiento contrario, así el historial del folio queda completo.
    ///
    /// @param idReserva id de la reserva
    /// @return el movimiento contrario que se registró
    /// @throws NoEncontradoExcepcion si la reserva no existe
    /// @throws ReglaNegocioExcepcion si el folio está cerrado o si no hay operaciones para deshacer
    public MovimientoFolio deshacerUltimo(String idReserva) {
        Reserva reserva = reservas.buscar(idReserva);
        exigirAbierto(reserva);
        return reserva.obtenerFolio().deshacerUltimo(tiempo.ahora());
    }

    /// Impide mover el folio de una reserva cancelada o no presentada. Una reserva finalizada sí lo permite.
    ///
    /// @param reserva reserva cuyo folio se quiere modificar
    /// @throws ReglaNegocioExcepcion si la reserva está CANCELADA o NO_PRESENTADO
    private void exigirAbierto(Reserva reserva) {
        EstadoReserva estado = reserva.obtenerEstado();
        if (estado == EstadoReserva.CANCELADA || estado == EstadoReserva.NO_PRESENTADO) {
            throw new ReglaNegocioExcepcion("El folio de la reserva " + reserva.obtenerId() + " está cerrado ("
                    + estado.nombreMostrado() + ")");
        }
    }
}
