package co.edu.uq.estructuras.sga.api.dto;

import co.edu.uq.estructuras.sga.core.apartamentos.EstadoApartamento;
import co.edu.uq.estructuras.sga.core.reservas.CanalReserva;
import co.edu.uq.estructuras.sga.core.tarifas.Temporada;


import java.time.LocalDate;

/// Cuerpos de respuesta del calendario de ocupación y de las estadísticas.
public final class DtosReportes {

    /// Contenedor de registros: no se instancia.
    private DtosReportes() {
    }

    /// Ocupación de un apartamento en el periodo (noches ocupadas sobre noches totales).
    public record OcupacionApartamentoDto(String codigo, String nombre, int nochesOcupadas, int nochesTotales, double tarifa) {
    }

    /// Ocupación de todo el alojamiento en una temporada.
    public record OcupacionTemporadaDto(Temporada temporada, String etiquetaTemporada, int nochesOcupadas, int nochesDisponibles,
                                     double tarifa) {
    }

    /// Ingresos de un mes.
    public record IngresosPeriodoDto(String periodo, long ingresos) {
    }

    /// Cantidad de reservas de un canal por estado.
    public record ResumenCanalDto(CanalReserva canal, String etiquetaCanal, int total, int pendiente,
                                    int confirmada, int enCurso, int finalizada, int cancelada, int noPresentado) {
    }

    /// Reporte de estadísticas completo del periodo consultado.
    public record ReporteDto(LocalDate desde, LocalDate hasta, OcupacionApartamentoDto[] ocupacionPorApartamento,
                            OcupacionTemporadaDto[] ocupacionPorTemporada, IngresosPeriodoDto[] ingresosPorMes,
                            long ingresosTotales, ResumenCanalDto[] reservasPorCanal, int conflictosCanal,
                            DtosOperaciones.ConflictoCanalDto[] ultimosConflictos, int preparacionesTerminadas,
                            double minutosPromedioPreparacion, int preparacionesEnCola) {
    }

    /// Celda del calendario: estado de una noche para un apartamento.
    public record CeldaCalendarioDto(LocalDate noche, String estado, String idReserva, String detalle) {
    }

    /// Fila del calendario: un apartamento y sus noches del mes.
    public record FilaCalendarioDto(String codigoApartamento, String nombreApartamento, EstadoApartamento estado,
                                 CeldaCalendarioDto[] celdas) {
    }

    /// Calendario de ocupación del mes.
    public record CalendarioDto(String mes, LocalDate[] dias, FilaCalendarioDto[] filas) {
    }
}
