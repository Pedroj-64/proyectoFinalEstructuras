package co.edu.uq.estructuras.sga.api;

import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones;
import co.edu.uq.estructuras.sga.api.dto.MapeadorDto;
import co.edu.uq.estructuras.sga.core.tarifas.PeriodoTemporada;
import co.edu.uq.estructuras.sga.core.tarifas.Temporada;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.service.ServicioTemporadas;

import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.PeriodoTemporadaDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.TemporadaDeDto;

import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/// Endpoints del calendario de temporadas.
@RestController
@RequestMapping("/api/temporadas")
public class ControladorTemporadas {

    private final ServicioTemporadas temporadas;

    /// Recibe por inyección el servicio de temporadas.
    public ControladorTemporadas(ServicioTemporadas temporadas) {
        this.temporadas = temporadas;
    }

    /// Periodos de temporada actuales.
    @GetMapping
    public PeriodoTemporadaDto[] periodos() {
        return MapeadorDto.aArreglo(temporadas.periodos(), MapeadorDto::aDto, PeriodoTemporadaDto[]::new);
    }

    /// Reemplaza todos los periodos de temporada (todo o nada).
    @PutMapping
    public PeriodoTemporadaDto[] reemplazar(@Valid @RequestBody PeriodoTemporadaDto[] periodos) {
        ListaDoblementeEnlazada<PeriodoTemporada> interpretado = new ListaDoblementeEnlazada<>();
        for (PeriodoTemporadaDto dto : periodos) {
            interpretado.agregarUltimo(new PeriodoTemporada(dto.temporada(), dto.inicio(), dto.fin()));
        }
        temporadas.reemplazarPeriodos(interpretado);
        return periodos();
    }

    /// Temporada de una fecha.
    @GetMapping("/de")
    public TemporadaDeDto temporadaDe(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        Temporada temporada = temporadas.temporadaDe(fecha);
        return new TemporadaDeDto(fecha, temporada, temporada.nombreMostrado());
    }
}
