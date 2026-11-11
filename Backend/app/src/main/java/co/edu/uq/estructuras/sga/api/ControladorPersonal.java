package co.edu.uq.estructuras.sga.api;

import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones;
import co.edu.uq.estructuras.sga.api.dto.MapeadorDto;
import co.edu.uq.estructuras.sga.service.ServicioEmpleados;
import co.edu.uq.estructuras.sga.service.ServicioTurnos;

import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.EmpleadoDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.SolicitudEmpleado;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.TurnoDto;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/// Empleados (administrador) y turnos de recepción.
@RestController
@RequestMapping("/api")
public class ControladorPersonal {

    private final ServicioEmpleados empleados;
    private final ServicioTurnos turnos;

    /// Recibe por inyección los servicios de empleados y turnos.
    public ControladorPersonal(ServicioEmpleados empleados, ServicioTurnos turnos) {
        this.empleados = empleados;
        this.turnos = turnos;
    }

    /// Lista los empleados.
    @GetMapping("/empleados")
    public EmpleadoDto[] buscarTodos() {
        return MapeadorDto.aArreglo(empleados.buscarTodos(), MapeadorDto::aDto, EmpleadoDto[]::new);
    }

    /// Detalle de un empleado.
    @GetMapping("/empleados/{id}")
    public EmpleadoDto buscar(@PathVariable String id) {
        return MapeadorDto.aDto(empleados.buscar(id));
    }

    /// Registra un empleado.
    @PostMapping("/empleados")
    @ResponseStatus(HttpStatus.CREATED)
    public EmpleadoDto crear(@Valid @RequestBody SolicitudEmpleado solicitud) {
        return MapeadorDto.aDto(empleados.crear(solicitud.id(), solicitud.nombreCompleto(), solicitud.rol(), solicitud.correo(),
                solicitud.telefono()));
    }

    /// Actualiza un empleado.
    @PutMapping("/empleados/{id}")
    public EmpleadoDto actualizar(@PathVariable String id, @Valid @RequestBody SolicitudEmpleado solicitud) {
        return MapeadorDto.aDto(empleados.actualizar(id, solicitud.nombreCompleto(), solicitud.rol(), solicitud.correo(),
                solicitud.telefono()));
    }

    /// Elimina un empleado.
    @DeleteMapping("/empleados/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        empleados.eliminar(id);
    }

    /// Turno de recepción en curso.
    @GetMapping("/turnos/actual")
    public TurnoDto turnoActual() {
        return MapeadorDto.aDto(turnos.actual(empleados.recepcionistas()));
    }

    /// Turno actual y los siguientes.
    @GetMapping("/turnos/proximos")
    public TurnoDto[] proximos(@RequestParam(defaultValue = "6") int cantidad) {
        return MapeadorDto.aArreglo(turnos.proximos(empleados.recepcionistas(), Math.min(cantidad, 21)), MapeadorDto::aDto, TurnoDto[]::new);
    }
}
