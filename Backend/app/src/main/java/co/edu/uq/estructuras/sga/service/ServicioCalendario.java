package co.edu.uq.estructuras.sga.service;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.apartamentos.EstadoApartamento;
import co.edu.uq.estructuras.sga.core.reservas.EstadoReserva;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.core.reservas.Reserva;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.repository.RepositorioApartamentos;
import co.edu.uq.estructuras.sga.repository.RepositorioReservas;


import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.stereotype.Service;

/// Calendario de ocupación: apartamentos × noches de un mes.
@Service
public class ServicioCalendario {

    /// Estado de una noche en el calendario: libre, ocupada por una reserva o bloqueada (fuera de servicio).
    public enum EstadoNoche {
        LIBRE, OCUPADO, BLOQUEADA
    }

    /// Estado de un apartamento en una noche.
    public record CeldaCalendario(LocalDate noche, EstadoNoche estado, String idReserva, String detalle) {
    }

    /// Fila del calendario: un apartamento y sus noches del mes.
    public record FilaCalendario(Apartamento apartamento, ListaDoblementeEnlazada<CeldaCalendario> celdas) {
    }

    private final RepositorioApartamentos apartamentos;
    private final RepositorioReservas reservas;

    /// Recibe por inyección los repositorios de apartamentos y reservas.
    public ServicioCalendario(RepositorioApartamentos apartamentos, RepositorioReservas reservas) {
        this.apartamentos = apartamentos;
        this.reservas = reservas;
    }

    /// Calendario de todo el mes, una fila por apartamento.
    public ListaDoblementeEnlazada<FilaCalendario> mes(YearMonth mes) {
        Estancia mesCompleto = new Estancia(mes.atDay(1), mes.plusMonths(1).atDay(1));
        ListaDoblementeEnlazada<FilaCalendario> filas = new ListaDoblementeEnlazada<>();
        for (Apartamento apartamento : apartamentos.buscarTodos()) {
            ListaDoblementeEnlazada<Reserva> enMes = reservas.buscarQueSeCruzan(apartamento.obtenerCodigo(), mesCompleto)
                    .filtrar(r -> r.obtenerEstado() != EstadoReserva.CANCELADA && r.obtenerEstado() != EstadoReserva.NO_PRESENTADO);
            ListaDoblementeEnlazada<CeldaCalendario> celdas = new ListaDoblementeEnlazada<>();
            for (LocalDate noche : mesCompleto) {
                celdas.agregarUltimo(celdaPara(apartamento, noche, enMes));
            }
            filas.agregarUltimo(new FilaCalendario(apartamento, celdas));
        }
        return filas;
    }

    /// Ocupada: estancia confirmada, en curso o finalizada. Bloqueada: reserva pendiente que
    /// retiene la noche o apartamento fuera de servicio. Libre: todo lo demás.
    private CeldaCalendario celdaPara(Apartamento apartamento, LocalDate noche, ListaDoblementeEnlazada<Reserva> reservasEnMes) {
        Reserva reserva = reservasEnMes.buscar(r -> r.obtenerEstancia().incluyeNoche(noche));
        if (reserva != null) {
            EstadoNoche estado = reserva.obtenerEstado() == EstadoReserva.PENDIENTE ? EstadoNoche.BLOQUEADA : EstadoNoche.OCUPADO;
            return new CeldaCalendario(noche, estado, reserva.obtenerId(),
                    reserva.obtenerTitular().obtenerNombreCompleto() + " · " + reserva.obtenerEstado().nombreMostrado());
        }
        if (apartamento.obtenerEstado() == EstadoApartamento.FUERA_DE_SERVICIO && apartamento.obtenerFueraDeServicioDesde() != null
                && !noche.isBefore(apartamento.obtenerFueraDeServicioDesde())) {
            return new CeldaCalendario(noche, EstadoNoche.BLOQUEADA, null, "Fuera de servicio: " + apartamento.obtenerMotivoFueraDeServicio());
        }
        return new CeldaCalendario(noche, EstadoNoche.LIBRE, null, null);
    }
}
