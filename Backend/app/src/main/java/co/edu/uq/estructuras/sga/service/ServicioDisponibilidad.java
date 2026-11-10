package co.edu.uq.estructuras.sga.service;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.apartamentos.ComparadoresApartamento;
import co.edu.uq.estructuras.sga.core.apartamentos.CriterioOrden;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.core.tarifas.CalendarioTemporadas;
import co.edu.uq.estructuras.sga.core.tarifas.LiquidadorEstancia;
import co.edu.uq.estructuras.sga.core.tarifas.Temporada;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.repository.RepositorioApartamentos;
import co.edu.uq.estructuras.sga.repository.RepositorioReservas;

import java.util.Comparator;
import org.springframework.stereotype.Service;

/// Búsqueda de disponibilidad: apartamentos libres para un rango de fechas y un número de
/// personas, ordenados con el [Comparator] del criterio elegido (patrón Strategy).
@Service
public class ServicioDisponibilidad {

    private final RepositorioApartamentos apartamentos;
    private final RepositorioReservas reservas;
    private final CalendarioTemporadas temporadas;
    private final LiquidadorEstancia liquidador;

    /// Recibe por inyección los repositorios, el calendario de temporadas y el liquidador.
    ///
    /// @param apartamentos repositorio de apartamentos
    /// @param reservas repositorio de reservas
    /// @param temporadas calendario que dice la temporada de cada fecha
    /// @param liquidador calcula el precio estimado de una estancia
    public ServicioDisponibilidad(RepositorioApartamentos apartamentos, RepositorioReservas reservas,
                               CalendarioTemporadas temporadas, LiquidadorEstancia liquidador) {
        this.apartamentos = apartamentos;
        this.reservas = reservas;
        this.temporadas = temporadas;
        this.liquidador = liquidador;
    }

    /// Caso de uso: saber qué apartamentos se pueden reservar para una estancia. Un apartamento sirve si
    /// es reservable (no está fuera de servicio), tiene capacidad para `huespedes` y no tiene una reserva
    /// activa que se cruce con la estancia. También lo usa [ServicioReservas] para reservar grupos.
    ///
    /// @param estancia fechas de entrada y salida
    /// @param huespedes número de personas
    /// @return los apartamentos disponibles, en el orden en que los entrega el repositorio
    public ListaDoblementeEnlazada<Apartamento> apartamentosDisponibles(Estancia estancia, int huespedes) {
        return apartamentos.buscarTodos().filtrar(apartamento -> apartamento.esReservable()
                && apartamento.obtenerCapacidad() >= huespedes
                && reservas.buscarSolapada(apartamento.obtenerCodigo(), estancia) == null);
    }

    /// Caso de uso: buscar disponibilidad desde el portal. Igual que [#apartamentosDisponibles], pero cada
    /// resultado trae la tarifa de la primera noche, el total estimado y cuántas reservas tiene el
    /// apartamento, y la lista sale ordenada según el criterio elegido. Si `huespedes` es menor que 1,
    /// se busca para 1 persona.
    ///
    /// @param estancia fechas de entrada y salida
    /// @param huespedes número de personas
    /// @param criterio criterio de orden; si es `null` se ordena por precio
    /// @return los apartamentos disponibles con su precio, ordenados según `criterio`
    public ListaDoblementeEnlazada<ApartamentoDisponible> buscar(Estancia estancia, int huespedes, CriterioOrden criterio) {
        Temporada temporadaPrimeraNoche = temporadas.temporadaDe(estancia.ingreso());
        ListaDoblementeEnlazada<Apartamento> ordenada = apartamentosDisponibles(estancia, Math.max(huespedes, 1))
                .ordenada(comparadorPara(criterio, temporadaPrimeraNoche));
        ListaDoblementeEnlazada<ApartamentoDisponible> resultado = new ListaDoblementeEnlazada<>();
        for (Apartamento apartamento : ordenada) {
            resultado.agregarUltimo(new ApartamentoDisponible(apartamento, apartamento.tarifaPara(temporadaPrimeraNoche),
                    liquidador.estimado(apartamento, estancia, Math.max(huespedes, 1)),
                    reservas.contarPorApartamento(apartamento.obtenerCodigo())));
        }
        return resultado;
    }

    /// Escoge el comparador según el criterio pedido (Strategy); el de precio usa la tarifa de la temporada consultada.
    ///
    /// @param criterio criterio de orden, o `null` para ordenar por precio
    /// @param temporada temporada de la primera noche
    /// @return el comparador que corresponde al criterio
    private Comparator<Apartamento> comparadorPara(CriterioOrden criterio, Temporada temporada) {
        return switch (criterio == null ? CriterioOrden.PRECIO : criterio) {
            case PRECIO -> ComparadoresApartamento.porPrecio(temporada);
            case CAPACIDAD -> ComparadoresApartamento.porCapacidad();
            case HABITACIONES -> ComparadoresApartamento.porHabitaciones();
            case RESERVAS -> ComparadoresApartamento.porCantidadReservas(
                    apartamento -> reservas.contarPorApartamento(apartamento.obtenerCodigo()));
        };
    }
}
