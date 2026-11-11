package co.edu.uq.estructuras.sga.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.ConflictoCanalDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.EnvioSolicitudExterna;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.ResultadoProcesamientoDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.SolicitudExternaDto;
import co.edu.uq.estructuras.sga.api.dto.MapeadorDto;
import co.edu.uq.estructuras.sga.core.canal.SolicitudReservaExterna;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.service.ServicioCanalExterno;
import co.edu.uq.estructuras.sga.service.ServicioReloj;
import jakarta.validation.Valid;

/// Endpoints del canal externo: plataforma simulada que envía solicitudes, cola y conflictos.
@RestController
@RequestMapping("/api/canal-externo")
public class ControladorCanalExterno {

    private final ServicioCanalExterno canal;
    private final ServicioReloj reloj;

    /// Recibe por inyección el servicio del canal y el reloj del sistema.
    public ControladorCanalExterno(ServicioCanalExterno canal, ServicioReloj reloj) {
        this.canal = canal;
        this.reloj = reloj;
    }

    /// La plataforma envía una solicitud; queda en la cola.
    @PostMapping("/solicitudes")
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudExternaDto enviar(@Valid @RequestBody EnvioSolicitudExterna envio) {
        SolicitudReservaExterna solicitud = new SolicitudReservaExterna(canal.siguienteIdSolicitud(), envio.plataforma(),
                envio.codigoApartamento().trim().toUpperCase(), new Estancia(envio.ingreso(), envio.salida()),
                MapeadorDto.aPerfil(envio.titular()), MapeadorDto.aOcupantes(envio.ocupantes()), reloj.ahora());
        canal.recibir(solicitud);
        return MapeadorDto.aDto(solicitud);
    }

    /// Solicitudes que esperan, de la más antigua a la más reciente.
    @GetMapping("/solicitudes")
    public SolicitudExternaDto[] pendientes() {
        return MapeadorDto.aArreglo(canal.pendientes(), MapeadorDto::aDto, SolicitudExternaDto[]::new);
    }

    /// Procesa solo la solicitud más antigua; responde 204 si la cola está vacía.
    @PostMapping("/procesar-siguiente")
    public ResponseEntity<ResultadoProcesamientoDto> procesarSiguiente() {
        ServicioCanalExterno.ResultadoProcesamiento resultado = canal.procesarSiguiente(reloj.ahora());
        return resultado == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(MapeadorDto.aDto(resultado));
    }

    /// Resultados de las solicitudes ya procesadas.
    @GetMapping("/resultados")
    public ResultadoProcesamientoDto[] resultados() {
        return MapeadorDto.aArreglo(canal.resultados(), MapeadorDto::aDto, ResultadoProcesamientoDto[]::new);
    }

    /// Procesa toda la cola y devuelve el resultado de cada solicitud.
    @PostMapping("/procesar")
    public ResultadoProcesamientoDto[] procesar() {
        return MapeadorDto.aArreglo(canal.procesarTodas(reloj.ahora()), MapeadorDto::aDto, ResultadoProcesamientoDto[]::new);
    }

    /// Solicitudes rechazadas.
    @GetMapping("/conflictos")
    public ConflictoCanalDto[] conflictos() {
        return MapeadorDto.aArreglo(canal.conflictos(), MapeadorDto::aDto, ConflictoCanalDto[]::new);
    }
}
