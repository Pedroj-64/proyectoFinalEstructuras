package co.edu.uq.estructuras.sga.service;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.canal.ConflictoCanal;
import co.edu.uq.estructuras.sga.core.folio.MovimientoFolio;
import co.edu.uq.estructuras.sga.core.reservas.CanalReserva;
import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.reservas.EstadoReserva;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.core.reservas.Reserva;
import co.edu.uq.estructuras.sga.core.tarifas.CalendarioTemporadas;
import co.edu.uq.estructuras.sga.core.tarifas.Temporada;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.estructuras.MapaHash;
import co.edu.uq.estructuras.sga.excepciones.RangoFechasInvalidoExcepcion;
import co.edu.uq.estructuras.sga.repository.RepositorioApartamentos;
import co.edu.uq.estructuras.sga.repository.RepositorioReservas;


import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.stereotype.Service;

/// Estadísticas calculadas bajo demanda recorriendo las estructuras (árboles, listas, tablas).
@Service
public class ServicioReportes {

    /// Ocupación de un apartamento en el periodo.
    public record OcupacionApartamento(String codigo, String nombre, int nochesOcupadas, int nochesTotales, double tarifa) {
    }

    /// Ocupación agregada de una temporada en el periodo.
    public record OcupacionTemporada(Temporada temporada, int nochesOcupadas, int nochesDisponibles, double tarifa) {
    }

    /// Ingresos recibidos en un mes.
    public record IngresosPeriodo(YearMonth periodo, Dinero ingresos) {
    }

    /// Conteo de reservas de un canal por estado.
    public record ResumenCanal(CanalReserva canal, int total, int pendiente, int confirmada, int enCurso,
                                 int finalizada, int cancelada, int noPresentado) {
    }

    /// Reporte completo que muestra la pantalla de estadísticas.
    public record Resumen(LocalDate desde, LocalDate hasta,
                          ListaDoblementeEnlazada<OcupacionApartamento> ocupacionPorApartamento,
                          ListaDoblementeEnlazada<OcupacionTemporada> ocupacionPorTemporada,
                          ListaDoblementeEnlazada<IngresosPeriodo> ingresosPorMes, Dinero ingresosTotales,
                          ListaDoblementeEnlazada<ResumenCanal> reservasPorCanal,
                          int conflictosCanal, ListaDoblementeEnlazada<ConflictoCanal> ultimosConflictos,
                          int preparacionesTerminadas, double minutosPromedioPreparacion, int preparacionesEnCola) {
    }

    private final RepositorioApartamentos apartamentos;
    private final RepositorioReservas reservas;
    private final CalendarioTemporadas temporadas;
    private final ServicioPreparacion preparacion;
    private final ServicioCanalExterno canalExterno;

    /// Recibe por inyección los repositorios, el calendario de temporadas y los servicios de preparación y canal externo.
    public ServicioReportes(RepositorioApartamentos apartamentos, RepositorioReservas reservas, CalendarioTemporadas temporadas,
                         ServicioPreparacion preparacion, ServicioCanalExterno canalExterno) {
        this.apartamentos = apartamentos;
        this.reservas = reservas;
        this.temporadas = temporadas;
        this.preparacion = preparacion;
        this.canalExterno = canalExterno;
    }

    /// Arma el reporte completo para el periodo `desde`–`hasta` (ambos incluidos).
    public Resumen resumen(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            throw new RangoFechasInvalidoExcepcion("El periodo del reporte es inválido");
        }
        Estancia periodo = new Estancia(desde, hasta.plusDays(1)); // `hasta` se incluye en los reportes
        ListaDoblementeEnlazada<ConflictoCanal> conflictos = canalExterno.conflictos();
        ListaDoblementeEnlazada<ConflictoCanal> ultimos = new ListaDoblementeEnlazada<>();
        for (ConflictoCanal conflicto : conflictos.reversa()) {
            if (ultimos.tamano() == 10) {
                break;
            }
            ultimos.agregarUltimo(conflicto);
        }
        ListaDoblementeEnlazada<IngresosPeriodo> ingresos = ingresosPorMes(desde, hasta);
        Dinero ingresosTotales = Dinero.CERO;
        for (IngresosPeriodo mes : ingresos) {
            ingresosTotales = ingresosTotales.mas(mes.ingresos());
        }
        return new Resumen(desde, hasta, ocupacionPorApartamento(periodo), ocupacionPorTemporada(periodo), ingresos, ingresosTotales,
                reservasPorCanal(), conflictos.tamano(), ultimos, preparacion.preparacionesTerminadas(),
                preparacion.minutosPromedio(), preparacion.preparacionesEnCola());
    }

    /// Noches ocupadas sobre noches disponibles, por apartamento.
    public ListaDoblementeEnlazada<OcupacionApartamento> ocupacionPorApartamento(Estancia periodo) {
        ListaDoblementeEnlazada<OcupacionApartamento> resultado = new ListaDoblementeEnlazada<>();
        int nochesTotales = (int) periodo.noches();
        for (Apartamento apartamento : apartamentos.buscarTodos()) {
            int ocupadas = 0;
            for (Reserva reserva : reservas.buscarQueSeCruzan(apartamento.obtenerCodigo(), periodo)) {
                if (cuentaComoOcupada(reserva)) {
                    ocupadas += nochesDentro(reserva.obtenerEstancia(), periodo);
                }
            }
            resultado.agregarUltimo(new OcupacionApartamento(apartamento.obtenerCodigo(), apartamento.obtenerNombre(), ocupadas, nochesTotales,
                    proporcion(ocupadas, nochesTotales)));
        }
        return resultado;
    }

    /// Noches ocupadas sobre noches disponibles, por temporada.
    public ListaDoblementeEnlazada<OcupacionTemporada> ocupacionPorTemporada(Estancia periodo) {
        MapaHash<Temporada, int[]> contadores = new MapaHash<>(); // [ocupadas, disponibles]
        int cantidadApartamentos = apartamentos.cantidad();
        for (LocalDate noche : periodo) {
            contadores.calcularSiAusente(temporadas.temporadaDe(noche), () -> new int[2])[1] += cantidadApartamentos;
        }
        for (Apartamento apartamento : apartamentos.buscarTodos()) {
            for (Reserva reserva : reservas.buscarQueSeCruzan(apartamento.obtenerCodigo(), periodo)) {
                if (!cuentaComoOcupada(reserva)) {
                    continue;
                }
                for (LocalDate noche : reserva.obtenerEstancia()) {
                    if (periodo.incluyeNoche(noche)) {
                        contadores.calcularSiAusente(temporadas.temporadaDe(noche), () -> new int[2])[0]++;
                    }
                }
            }
        }
        ListaDoblementeEnlazada<OcupacionTemporada> resultado = new ListaDoblementeEnlazada<>();
        for (Temporada temporada : Temporada.values()) {
            int[] valores = contadores.obtenerOPorDefecto(temporada, new int[2]);
            resultado.agregarUltimo(new OcupacionTemporada(temporada, valores[0], valores[1], proporcion(valores[0], valores[1])));
        }
        return resultado;
    }

    /// Dinero realmente recibido por mes: pagos menos pagos revertidos y reembolsos.
    public ListaDoblementeEnlazada<IngresosPeriodo> ingresosPorMes(LocalDate desde, LocalDate hasta) {
        MapaHash<YearMonth, Dinero> porMes = new MapaHash<>();
        for (Reserva reserva : reservas.buscarTodos()) {
            for (MovimientoFolio movimiento : reserva.obtenerFolio().obtenerMovimientos()) {
                LocalDate dia = movimiento.fechaCreacion().toLocalDate();
                if (dia.isBefore(desde) || dia.isAfter(hasta)) {
                    continue;
                }
                Dinero delta = switch (movimiento.tipo()) {
                    case PAGO -> movimiento.monto();
                    case ANULACION_PAGO, REEMBOLSO -> movimiento.monto().negar();
                    case CARGO, ANULACION_CARGO -> Dinero.CERO;
                };
                YearMonth mes = YearMonth.from(dia);
                porMes.poner(mes, porMes.obtenerOPorDefecto(mes, Dinero.CERO).mas(delta));
            }
        }
        ListaDoblementeEnlazada<IngresosPeriodo> resultado = new ListaDoblementeEnlazada<>();
        for (YearMonth mes = YearMonth.from(desde); !mes.isAfter(YearMonth.from(hasta)); mes = mes.plusMonths(1)) {
            resultado.agregarUltimo(new IngresosPeriodo(mes, porMes.obtenerOPorDefecto(mes, Dinero.CERO)));
        }
        return resultado;
    }

    /// Reservas por canal y estado.
    public ListaDoblementeEnlazada<ResumenCanal> reservasPorCanal() {
        ListaDoblementeEnlazada<ResumenCanal> resultado = new ListaDoblementeEnlazada<>();
        ListaDoblementeEnlazada<Reserva> todos = reservas.buscarTodos();
        for (CanalReserva canal : CanalReserva.values()) {
            ListaDoblementeEnlazada<Reserva> deCanal = todos.filtrar(r -> r.obtenerCanal() == canal);
            resultado.agregarUltimo(new ResumenCanal(canal, deCanal.tamano(),
                    contarEstado(deCanal, EstadoReserva.PENDIENTE), contarEstado(deCanal, EstadoReserva.CONFIRMADA),
                    contarEstado(deCanal, EstadoReserva.EN_CURSO), contarEstado(deCanal, EstadoReserva.FINALIZADA),
                    contarEstado(deCanal, EstadoReserva.CANCELADA), contarEstado(deCanal, EstadoReserva.NO_PRESENTADO)));
        }
        return resultado;
    }

    /// Cuenta las reservas de la lista que están en el estado dado.
    private static int contarEstado(ListaDoblementeEnlazada<Reserva> lista, EstadoReserva estado) {
        return lista.cantidadSiCumplen(r -> r.obtenerEstado() == estado);
    }

    /// Indica si la reserva ocupa noches para la estadística (confirmada, en curso o finalizada).
    private static boolean cuentaComoOcupada(Reserva reserva) {
        EstadoReserva estado = reserva.obtenerEstado();
        return estado == EstadoReserva.CONFIRMADA || estado == EstadoReserva.EN_CURSO
                || estado == EstadoReserva.FINALIZADA;
    }

    /// Cuenta cuántas noches de la estancia caen dentro del periodo consultado.
    private static int nochesDentro(Estancia estancia, Estancia periodo) {
        int noches = 0;
        for (LocalDate noche : estancia) {
            if (periodo.incluyeNoche(noche)) {
                noches++;
            }
        }
        return noches;
    }

    /// Porcentaje con un decimal (`parte / completo × 100`); devuelve 0 si `completo` es 0.
    private static double proporcion(int parte, int completo) {
        return completo == 0 ? 0 : Math.round(parte * 1000.0 / completo) / 10.0;
    }
}
