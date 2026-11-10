package co.edu.uq.estructuras.sga.api;

import co.edu.uq.estructuras.sga.api.dto.DtosFolio.FolioDto;
import co.edu.uq.estructuras.sga.api.dto.DtosFolio.SolicitudMovimiento;
import co.edu.uq.estructuras.sga.api.dto.MapeadorDto;
import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.service.ServicioFolio;
import co.edu.uq.estructuras.sga.service.ServicioReservas;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// Endpoints del folio de una reserva: consultar, registrar cargos y pagos, y deshacer.
/// Todas las rutas empiezan por `/api/reservas/{id}/folio`, y cada operación devuelve el folio actualizado.
@RestController
@RequestMapping("/api/reservas/{id}/folio")
public class ControladorFolio {

    private final ServicioFolio folios;
    private final ServicioReservas reservas;

    /// Recibe por inyección los servicios de folio y de reservas.
    ///
    /// @param folios servicio que registra cargos, pagos y deshacer
    /// @param reservas servicio para buscar la reserva dueña del folio
    public ControladorFolio(ServicioFolio folios, ServicioReservas reservas) {
        this.folios = folios;
        this.reservas = reservas;
    }

    /// `GET /api/reservas/{id}/folio`: devuelve el folio con todos sus movimientos, los totales, el saldo
    /// y cuál sería la próxima operación que se puede deshacer.
    ///
    /// @param id id de la reserva
    /// @return el folio de la reserva
    @GetMapping
    public FolioDto folio(@PathVariable String id) {
        return MapeadorDto.aFolioDto(reservas.buscar(id));
    }

    /// `POST /api/reservas/{id}/folio/cargos`: registra un cargo de recepción (minibar, lavandería...).
    ///
    /// @param id id de la reserva
    /// @param solicitud monto (mayor que cero) y descripción del cargo
    /// @return el folio actualizado
    @PostMapping("/cargos")
    public FolioDto cargo(@PathVariable String id, @Valid @RequestBody SolicitudMovimiento solicitud) {
        folios.agregarCargo(id, Dinero.de(solicitud.monto()), solicitud.descripcion());
        return folio(id);
    }

    /// `POST /api/reservas/{id}/folio/pagos`: registra un pago; si la reserva estaba pendiente y el pago cubre
    /// el anticipo, queda confirmada.
    ///
    /// @param id id de la reserva
    /// @param solicitud monto (mayor que cero) y descripción del pago
    /// @return el folio actualizado
    @PostMapping("/pagos")
    public FolioDto pagar(@PathVariable String id, @Valid @RequestBody SolicitudMovimiento solicitud) {
        folios.agregarPago(id, Dinero.de(solicitud.monto()), solicitud.descripcion());
        return folio(id);
    }

    /// `POST /api/reservas/{id}/folio/deshacer`: deshace la última operación de recepción registrando su
    /// movimiento contrario (no borra nada).
    ///
    /// @param id id de la reserva
    /// @return el folio actualizado
    @PostMapping("/deshacer")
    public FolioDto deshacer(@PathVariable String id) {
        folios.deshacerUltimo(id);
        return folio(id);
    }
}
