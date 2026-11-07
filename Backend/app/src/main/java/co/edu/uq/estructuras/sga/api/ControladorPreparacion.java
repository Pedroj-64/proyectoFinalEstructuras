package co.edu.uq.estructuras.sga.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.EnPreparacionDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.RutaDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.TareaPreparacionDto;
import co.edu.uq.estructuras.sga.api.dto.MapeadorDto;
import co.edu.uq.estructuras.sga.excepciones.NoEncontradoExcepcion;
import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.apartamentos.EstadoApartamento;
import co.edu.uq.estructuras.sga.core.preparacion.PreparacionEnCurso;
import co.edu.uq.estructuras.sga.core.preparacion.TareaPreparacion;
import co.edu.uq.estructuras.sga.repository.RepositorioApartamentos;
import co.edu.uq.estructuras.sga.service.ServicioMapaEdificio;
import co.edu.uq.estructuras.sga.service.ServicioPreparacion;
import co.edu.uq.estructuras.sga.service.ServicioReloj;

/// Endpoints de la cola de preparación: ver la cola, iniciar y terminar preparaciones y ruta sugerida.
@RestController
@RequestMapping("/api/preparacion")
public class ControladorPreparacion {

    private final ServicioPreparacion preparacion;
    private final ServicioMapaEdificio mapaEdificio;
    private final RepositorioApartamentos apartamentos;
    private final ServicioReloj reloj;

    /// Recibe por inyección los servicios y el repositorio que usa.
    public ControladorPreparacion(ServicioPreparacion preparacion, ServicioMapaEdificio mapaEdificio,
                                  RepositorioApartamentos apartamentos, ServicioReloj reloj) {
        this.preparacion = preparacion;
        this.mapaEdificio = mapaEdificio;
        this.apartamentos = apartamentos;
        this.reloj = reloj;
    }

    /// Cola en orden de atención.
    @GetMapping("/cola")
    public TareaPreparacionDto[] cola() {
        int[] posicion = {0};
        return MapeadorDto.aArreglo(preparacion.colaOrdenada(), tarea -> {
            Apartamento apartamento = exigirApartamento(tarea.codigoApartamento());
            return aDto(++posicion[0], tarea, apartamento);
        }, TareaPreparacionDto[]::new);
    }

    /// Apartamentos que se están preparando.
    @GetMapping("/en-curso")
    public EnPreparacionDto[] enCurso() {
        return MapeadorDto.aArreglo(preparacion.enCurso(), this::aDto, EnPreparacionDto[]::new);
    }

    /// Ruta sugerida desde recepción: primero las llegadas de hoy, siempre al más cercano.
    @GetMapping("/ruta")
    public RutaDto ruta() {
        return MapeadorDto.aDto(mapaEdificio.rutaPor(preparacion.gruposPorPrioridad()));
    }

    /// Toma la tarea más urgente y deja el apartamento en preparación.
    @PostMapping("/iniciar")
    public EnPreparacionDto iniciar() {
        PreparacionEnCurso iniciada = preparacion.iniciarSiguiente(reloj.ahora());
        exigirApartamento(iniciada.codigoApartamento()).cambiarEstado(EstadoApartamento.EN_PREPARACION);
        return aDto(iniciada);
    }

    /// Termina la preparación y deja el apartamento listo.
    @PostMapping("/{codigo}/terminar")
    public void terminar(@PathVariable String codigo) {
        preparacion.terminar(codigo, reloj.ahora());
        exigirApartamento(codigo).cambiarEstado(EstadoApartamento.PREPARADO);
    }

    private TareaPreparacionDto aDto(int posicion, TareaPreparacion tarea, Apartamento apartamento) {
        return new TareaPreparacionDto(posicion, tarea.codigoApartamento(), apartamento.obtenerNombre(), apartamento.obtenerBloque(),
                tarea.prioridad(), tarea.llegadaHoy(), tarea.proximaLlegada(), tarea.fechaEncolado());
    }

    private EnPreparacionDto aDto(PreparacionEnCurso curso) {
        return new EnPreparacionDto(curso.codigoApartamento(), exigirApartamento(curso.codigoApartamento()).obtenerNombre(),
                curso.fechaInicio());
    }

    private Apartamento exigirApartamento(String codigo) {
        Apartamento apartamento = apartamentos.buscarPorCodigo(codigo);
        if (apartamento == null) {
            throw new NoEncontradoExcepcion("El apartamento", codigo);
        }
        return apartamento;
    }
}
