package co.edu.uq.estructuras.sga.api;

import co.edu.uq.estructuras.sga.api.dto.DtosApartamento;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva;
import co.edu.uq.estructuras.sga.api.dto.MapeadorDto;
import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.apartamentos.CriterioOrden;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.repository.RepositorioReservas;
import co.edu.uq.estructuras.sga.service.DatosApartamento;
import co.edu.uq.estructuras.sga.service.ServicioApartamentos;
import co.edu.uq.estructuras.sga.service.ServicioDisponibilidad;
import co.edu.uq.estructuras.sga.service.ServicioReservas;

import co.edu.uq.estructuras.sga.api.dto.DtosApartamento.ApartamentoDisponibleDto;
import co.edu.uq.estructuras.sga.api.dto.DtosApartamento.ApartamentoDto;
import co.edu.uq.estructuras.sga.api.dto.DtosApartamento.SolicitudApartamento;
import co.edu.uq.estructuras.sga.api.dto.DtosApartamento.SolicitudCambioEstado;
import co.edu.uq.estructuras.sga.api.dto.DtosApartamento.TarifasDto;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva.ReservaDto;

import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/// Endpoints del catálogo de apartamentos, sus tarifas, su estado y la búsqueda de disponibilidad.
@RestController
@RequestMapping("/api")
public class ControladorApartamentos {

    private final ServicioApartamentos apartamentos;
    private final ServicioDisponibilidad disponibilidad;
    private final ServicioReservas reservas;
    private final RepositorioReservas repositorioReservas;

    /// Recibe por inyección los servicios de apartamentos, disponibilidad y reservas y el repositorio de reservas.
    public ControladorApartamentos(ServicioApartamentos apartamentos, ServicioDisponibilidad disponibilidad,
                               ServicioReservas reservas, RepositorioReservas repositorioReservas) {
        this.apartamentos = apartamentos;
        this.disponibilidad = disponibilidad;
        this.reservas = reservas;
        this.repositorioReservas = repositorioReservas;
    }

    /// Lista el catálogo de apartamentos ordenado por código.
    @GetMapping("/apartamentos")
    public ApartamentoDto[] buscarTodos() {
        return MapeadorDto.aArreglo(apartamentos.buscarTodos(), this::aDto, ApartamentoDto[]::new);
    }

    /// Detalle de un apartamento.
    @GetMapping("/apartamentos/{codigo}")
    public ApartamentoDto buscar(@PathVariable String codigo) {
        return aDto(apartamentos.buscar(codigo));
    }

    /// Crea un apartamento (administrador).
    @PostMapping("/apartamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public ApartamentoDto crear(@Valid @RequestBody SolicitudApartamento solicitud) {
        return aDto(apartamentos.crear(aDatos(solicitud)));
    }

    /// Actualiza los datos de un apartamento (administrador).
    @PutMapping("/apartamentos/{codigo}")
    public ApartamentoDto actualizar(@PathVariable String codigo, @Valid @RequestBody SolicitudApartamento solicitud) {
        return aDto(apartamentos.actualizar(codigo, aDatos(solicitud)));
    }

    /// Cambia las tarifas por persona de un apartamento.
    @PutMapping("/apartamentos/{codigo}/tarifas")
    public ApartamentoDto actualizarTarifas(@PathVariable String codigo, @Valid @RequestBody TarifasDto tarifas) {
        return aDto(apartamentos.actualizarTarifas(codigo, MapeadorDto.aTarifas(tarifas)));
    }

    /// Cambio manual de estado (fuera de servicio y reactivación).
    @PatchMapping("/apartamentos/{codigo}/estado")
    public ApartamentoDto cambiarEstado(@PathVariable String codigo, @Valid @RequestBody SolicitudCambioEstado solicitud) {
        return aDto(apartamentos.cambiarEstado(codigo, solicitud.estado(), solicitud.motivo()));
    }

    /// Elimina un apartamento sin reservas activas.
    @DeleteMapping("/apartamentos/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String codigo) {
        apartamentos.eliminar(codigo);
    }

    /// Reservas del apartamento en orden de fecha de entrada.
    @GetMapping("/apartamentos/{codigo}/reservas")
    public ReservaDto[] reservasDe(@PathVariable String codigo) {
        return MapeadorDto.aArreglo(reservas.buscarPorApartamento(codigo), MapeadorDto::aDto, ReservaDto[]::new);
    }

    /// Apartamentos libres para una estancia y un número de huéspedes, ordenados por el criterio elegido.
    @GetMapping("/disponibilidad")
    public ApartamentoDisponibleDto[] disponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ingreso,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salida,
            @RequestParam(defaultValue = "1") int huespedes,
            @RequestParam(defaultValue = "PRECIO") CriterioOrden ordenar) {
        return MapeadorDto.aArreglo(disponibilidad.buscar(new Estancia(ingreso, salida), huespedes, ordenar), MapeadorDto::aDto,
                ApartamentoDisponibleDto[]::new);
    }

    /// Convierte a DTO incluyendo el número de reservas del apartamento.
    private ApartamentoDto aDto(Apartamento apartamento) {
        return MapeadorDto.aDto(apartamento, a -> repositorioReservas.contarPorApartamento(a.obtenerCodigo()));
    }

    /// Traduce la petición JSON a los datos que entiende el servicio.
    private static DatosApartamento aDatos(SolicitudApartamento solicitud) {
        return new DatosApartamento(solicitud.codigo(), solicitud.nombre(), solicitud.bloque(), solicitud.piso(), solicitud.habitaciones(),
                solicitud.capacidad(), MapeadorDto.aLista(solicitud.caracteristicas()), MapeadorDto.aTarifas(solicitud.tarifas()));
    }
}
