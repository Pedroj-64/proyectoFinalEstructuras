package co.edu.uq.estructuras.sga.api;

import co.edu.uq.estructuras.sga.api.dto.DtosReserva;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva.CotizacionDto;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva.PlanGrupoDto;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva.ReservaDto;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva.SolicitudCotizacion;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva.VistaPreviaReembolsoDto;
import co.edu.uq.estructuras.sga.api.dto.MapeadorDto;
import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.reservas.AsignadorGrupos;
import co.edu.uq.estructuras.sga.core.reservas.CanalReserva;
import co.edu.uq.estructuras.sga.core.reservas.EstadoReserva;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.service.ServicioDisponibilidad;
import co.edu.uq.estructuras.sga.service.ServicioReservas;
import co.edu.uq.estructuras.sga.service.ServicioVencimientos;

import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/// Endpoints del ciclo de vida de la reserva: listar, crear, cotizar, cancelar, check-in, check-out y no-show.
/// Todas las rutas empiezan por `/api`.
///
/// Ojo con los nombres repetidos: `DtosReserva.SolicitudReserva` y `DtosReserva.SolicitudReservaGrupo` son lo
/// que llega por HTTP; `co.edu.uq.estructuras.sga.core.reservas.SolicitudReserva` y `SolicitudReservaGrupo`
/// son lo que recibe el servicio. Por eso aquí siempre se escriben con su nombre calificado.
@RestController
@RequestMapping("/api")
public class ControladorReservas {

    private final ServicioReservas reservas;
    private final ServicioVencimientos vencimientos;
    private final ServicioDisponibilidad disponibilidad;
    private final AsignadorGrupos asignador;

    /// Recibe por inyección los servicios de reservas, vencimientos y disponibilidad y el asignador de grupos.
    ///
    /// @param reservas servicio con el ciclo de vida de la reserva
    /// @param vencimientos servicio con la cola de reservas pendientes por vencer
    /// @param disponibilidad servicio que busca apartamentos libres
    /// @param asignador asignador de grupos con backtracking
    public ControladorReservas(ServicioReservas reservas, ServicioVencimientos vencimientos,
                                 ServicioDisponibilidad disponibilidad, AsignadorGrupos asignador) {
        this.reservas = reservas;
        this.vencimientos = vencimientos;
        this.disponibilidad = disponibilidad;
        this.asignador = asignador;
    }

    /// `GET /api/reservas`: lista las reservas, primero las más recientes. Se puede filtrar por estado y por canal.
    ///
    /// @param estado estado por el que se filtra (opcional)
    /// @param canal canal por el que se filtra (opcional)
    /// @return las reservas que cumplen los filtros
    @GetMapping("/reservas")
    public ReservaDto[] buscarTodos(@RequestParam(required = false) EstadoReserva estado,
                                    @RequestParam(required = false) CanalReserva canal) {
        return MapeadorDto.aArreglo(reservas.buscarTodos(estado, canal), MapeadorDto::aDto, ReservaDto[]::new);
    }

    /// `GET /api/reservas/{id}`: devuelve el detalle de una reserva, con su saldo, anticipo y vencimiento.
    ///
    /// @param id id de la reserva
    /// @return la reserva
    @GetMapping("/reservas/{id}")
    public ReservaDto buscar(@PathVariable String id) {
        return MapeadorDto.aDto(reservas.buscar(id));
    }

    /// `POST /api/reservas`: crea una reserva por el portal (huésped) o directa (recepción). Responde
    /// 201 Created. Las reservas externas no entran por aquí, sino por la cola del canal externo.
    ///
    /// @param solicitud datos de la reserva tal como llegan por HTTP
    /// @return la reserva creada
    /// @throws IllegalArgumentException si el canal es EXTERNO
    @PostMapping("/reservas")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaDto crear(@Valid @RequestBody DtosReserva.SolicitudReserva solicitud) {
        if (solicitud.canal() == CanalReserva.EXTERNO) {
            throw new IllegalArgumentException("Las reservas externas llegan por la cola del canal externo");
        }
        co.edu.uq.estructuras.sga.core.reservas.SolicitudReserva solicitudDominio =
                new co.edu.uq.estructuras.sga.core.reservas.SolicitudReserva(solicitud.codigoApartamento(),
                        solicitud.documentoTitular(), MapeadorDto.aOcupantes(solicitud.ocupantes()),
                        new Estancia(solicitud.ingreso(), solicitud.salida()), solicitud.canal(), solicitud.llegadaEstimada());
        return MapeadorDto.aDto(reservas.crear(solicitudDominio));
    }

    /// `GET /api/reservas/plan-grupal`: muestra qué apartamentos elegiría el backtracking para un grupo
    /// grande y cuántas camas suman. No reserva nada.
    ///
    /// @param ingreso fecha de entrada (formato ISO, `aaaa-mm-dd`)
    /// @param salida fecha de salida (formato ISO, `aaaa-mm-dd`)
    /// @param tamanoGrupo número de personas del grupo
    /// @return el tamaño del grupo, los códigos de los apartamentos elegidos y su capacidad total
    @GetMapping("/reservas/plan-grupal")
    public PlanGrupoDto planGrupo(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ingreso,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salida,
                                  @RequestParam int tamanoGrupo) {
        Estancia estancia = new Estancia(ingreso, salida);
        ListaDoblementeEnlazada<Apartamento> plan = asignador
                .asignar(disponibilidad.apartamentosDisponibles(estancia, 1), tamanoGrupo);
        int capacidad = 0;
        for (Apartamento apartamento : plan) {
            capacidad += apartamento.obtenerCapacidad();
        }
        return new PlanGrupoDto(tamanoGrupo, MapeadorDto.aArreglo(plan, Apartamento::obtenerCodigo, String[]::new), capacidad);
    }

    /// `POST /api/reservas/grupo`: reserva para un grupo grande en los apartamentos que elige el backtracking,
    /// una reserva por apartamento. Responde 201 Created.
    ///
    /// @param solicitud datos del grupo tal como llegan por HTTP
    /// @return las reservas creadas
    @PostMapping("/reservas/grupo")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaDto[] crearGrupo(@Valid @RequestBody DtosReserva.SolicitudReservaGrupo solicitud) {
        co.edu.uq.estructuras.sga.core.reservas.SolicitudReservaGrupo solicitudDominio =
                new co.edu.uq.estructuras.sga.core.reservas.SolicitudReservaGrupo(solicitud.documentoTitular(),
                        MapeadorDto.aOcupantes(solicitud.ocupantes()), new Estancia(solicitud.ingreso(), solicitud.salida()),
                        solicitud.canal(), solicitud.llegadaEstimada());
        return MapeadorDto.aArreglo(reservas.crearParaGrupo(solicitudDominio), MapeadorDto::aDto, ReservaDto[]::new);
    }

    /// `POST /api/cotizaciones`: cotiza una estancia noche por noche sin reservar.
    ///
    /// @param solicitud apartamento, fechas y ocupantes
    /// @return la cotización con el desglose por noche, el total y el anticipo
    @PostMapping("/cotizaciones")
    public CotizacionDto cotizacion(@Valid @RequestBody SolicitudCotizacion solicitud) {
        return MapeadorDto.aDto(solicitud.codigoApartamento(), reservas.cotizacion(solicitud.codigoApartamento(),
                new Estancia(solicitud.ingreso(), solicitud.salida()), MapeadorDto.aOcupantes(solicitud.ocupantes())));
    }

    /// `GET /api/reservas/{id}/vista-previa-reembolso`: muestra cuánto se devolvería si la reserva se cancela hoy,
    /// sin cancelarla.
    ///
    /// @param id id de la reserva
    /// @return lo pagado, el porcentaje y el valor que se devolverían, la penalidad y los días que faltan
    @GetMapping("/reservas/{id}/vista-previa-reembolso")
    public VistaPreviaReembolsoDto vistaPreviaReembolso(@PathVariable String id) {
        return MapeadorDto.aDto(reservas.vistaPreviaReembolso(id));
    }

    /// `POST /api/reservas/{id}/cancelar`: cancela la reserva y devuelve lo que corresponde según la política
    /// de reembolso.
    ///
    /// @param id id de la reserva
    /// @return el reembolso aplicado
    @PostMapping("/reservas/{id}/cancelar")
    public VistaPreviaReembolsoDto cancelar(@PathVariable String id) {
        return MapeadorDto.aDto(reservas.cancelar(id));
    }

    /// `POST /api/reservas/{id}/ingreso`: registra la llegada del huésped (check-in).
    ///
    /// @param id id de la reserva
    /// @return la reserva, ahora en curso
    @PostMapping("/reservas/{id}/ingreso")
    public ReservaDto ingreso(@PathVariable String id) {
        return MapeadorDto.aDto(reservas.ingreso(id));
    }

    /// `POST /api/reservas/{id}/salida`: registra la salida del huésped (check-out); exige el saldo en cero.
    ///
    /// @param id id de la reserva
    /// @return la reserva, ahora finalizada
    @PostMapping("/reservas/{id}/salida")
    public ReservaDto salida(@PathVariable String id) {
        return MapeadorDto.aDto(reservas.salida(id));
    }

    /// `POST /api/reservas/{id}/no-presentado`: marca la reserva como no presentada (no-show).
    ///
    /// @param id id de la reserva
    /// @return la reserva marcada como no presentada
    @PostMapping("/reservas/{id}/no-presentado")
    public ReservaDto noPresentado(@PathVariable String id) {
        return MapeadorDto.aDto(reservas.marcarNoPresentado(id));
    }

    /// `GET /api/reservas/vencimientos-pendientes`: lista las reservas pendientes que esperan el anticipo,
    /// primero las que vencen antes (sale de la cola de prioridad de [ServicioVencimientos]).
    ///
    /// @return las reservas pendientes ordenadas por vencimiento
    @GetMapping("/reservas/vencimientos-pendientes")
    public ReservaDto[] vencimientosPendientes() {
        return MapeadorDto.aArreglo(vencimientos.proximos(), MapeadorDto::aDto, ReservaDto[]::new);
    }
}
