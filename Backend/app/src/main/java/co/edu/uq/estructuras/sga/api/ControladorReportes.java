package co.edu.uq.estructuras.sga.api;

import co.edu.uq.estructuras.sga.api.dto.DtosReportes;
import co.edu.uq.estructuras.sga.api.dto.MapeadorDto;
import co.edu.uq.estructuras.sga.service.ServicioCalendario;
import co.edu.uq.estructuras.sga.service.ServicioReportes;

import co.edu.uq.estructuras.sga.api.dto.DtosReportes.CalendarioDto;
import co.edu.uq.estructuras.sga.api.dto.DtosReportes.FilaCalendarioDto;
import co.edu.uq.estructuras.sga.api.dto.DtosReportes.ReporteDto;

import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/// Calendario de ocupación y estadísticas.
@RestController
@RequestMapping("/api")
public class ControladorReportes {

    private final ServicioCalendario calendario;
    private final ServicioReportes reporta;

    /// Recibe por inyección los servicios de calendario y reportes.
    public ControladorReportes(ServicioCalendario calendario, ServicioReportes reporta) {
        this.calendario = calendario;
        this.reporta = reporta;
    }

    /// Calendario de ocupación de un mes (`yyyy-MM`).
    @GetMapping("/calendario")
    public CalendarioDto calendario(@RequestParam String mes) {
        YearMonth anioMes = YearMonth.parse(mes);
        LocalDate[] dias = new LocalDate[anioMes.lengthOfMonth()];
        for (int i = 0; i < dias.length; i++) {
            dias[i] = anioMes.atDay(i + 1);
        }
        return new CalendarioDto(anioMes.toString(), dias,
                MapeadorDto.aArreglo(calendario.mes(anioMes), MapeadorDto::aDto, FilaCalendarioDto[]::new));
    }

    /// Estadísticas del periodo indicado.
    @GetMapping("/reportes/resumen")
    public ReporteDto resumen(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return MapeadorDto.aDto(reporta.resumen(desde, hasta));
    }
}
