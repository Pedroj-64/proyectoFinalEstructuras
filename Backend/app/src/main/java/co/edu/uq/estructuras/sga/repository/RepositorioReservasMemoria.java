package co.edu.uq.estructuras.sga.repository;

import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.core.reservas.Reserva;
import co.edu.uq.estructuras.sga.estructuras.ArbolBinarioBusqueda;
import co.edu.uq.estructuras.sga.estructuras.GuiaBusqueda;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.estructuras.MapaHash;


import org.springframework.stereotype.Repository;

/// Cada apartamento tiene un árbol binario de búsqueda con sus reservas ordenadas por fecha de entrada.
/// Una tabla hash por id da acceso O(1) cuando se conoce el número de reserva.
@Repository
public class RepositorioReservasMemoria implements RepositorioReservas {

    /// Un árbol de reservas por apartamento, ordenado por fecha de entrada.
    private final MapaHash<String, ArbolBinarioBusqueda<Reserva>> porApartamento = new MapaHash<>();
    /// Índice por id para encontrar una reserva en O(1).
    private final MapaHash<String, Reserva> porId = new MapaHash<>();
    /// Consecutivo de ids de reserva.
    private int consecutivo;

    /// Siguiente número de reserva (R-0001, R-0002...).
    @Override
    public String siguienteId() {
        consecutivo++;
        return "R-%04d".formatted(consecutivo);
    }

    /// Guarda la reserva en el árbol de su apartamento y en el índice por id.
    @Override
    public void guardar(Reserva reserva) {
        if (porId.contieneLlave(reserva.obtenerId())) {
            return; // las reservas son entidades mutables ya referenciadas por el árbol
        }
        porId.poner(reserva.obtenerId(), reserva);
        porApartamento.calcularSiAusente(reserva.obtenerApartamento().obtenerCodigo(), ArbolBinarioBusqueda::new).insertar(reserva);
    }

    /// Búsqueda O(1) por id en la tabla hash.
    @Override
    public Reserva buscarPorId(String id) {
        return id == null ? null : porId.obtener(id.trim().toUpperCase());
    }

    /// Primera reserva activa solapada, buscada recursivamente con poda.
    @Override
    public Reserva buscarSolapada(String codigoApartamento, Estancia estancia) {
        ArbolBinarioBusqueda<Reserva> arbol = porApartamento.obtener(codigoApartamento);
        return arbol == null ? null : arbol.buscarPrimero(new GuiaSolapamiento(estancia, true));
    }

    /// Todas las reservas que comparten noches con la estancia, buscadas con poda.
    @Override
    public ListaDoblementeEnlazada<Reserva> buscarQueSeCruzan(String codigoApartamento, Estancia estancia) {
        ArbolBinarioBusqueda<Reserva> arbol = porApartamento.obtener(codigoApartamento);
        return arbol == null ? new ListaDoblementeEnlazada<>() : arbol.buscarTodos(new GuiaSolapamiento(estancia, false));
    }

    /// Reservas del apartamento en orden de fecha de entrada.
    @Override
    public ListaDoblementeEnlazada<Reserva> buscarPorApartamento(String codigoApartamento) {
        ArbolBinarioBusqueda<Reserva> arbol = porApartamento.obtener(codigoApartamento);
        return arbol == null ? new ListaDoblementeEnlazada<>() : arbol.enOrden();
    }

    /// Todas las reservas del sistema.
    @Override
    public ListaDoblementeEnlazada<Reserva> buscarTodos() {
        return porId.valores();
    }

    /// Cantidad de reservas del apartamento.
    @Override
    public int contarPorApartamento(String codigoApartamento) {
        ArbolBinarioBusqueda<Reserva> arbol = porApartamento.obtener(codigoApartamento);
        return arbol == null ? 0 : arbol.tamano();
    }

    /// Vacía el repositorio y reinicia la numeración.
    @Override
    public void vaciar() {
        porApartamento.vaciar();
        porId.vaciar();
        consecutivo = 0;
    }

    /// Guía la búsqueda recursiva en el árbol de reservas.
    ///
    /// Las reservas a la derecha de un nodo empiezan el mismo día o después. Si un nodo ya empieza
    /// en o después de la salida pedida, nada a su derecha puede solaparse, así que esa rama
    /// completa se poda.
    private record GuiaSolapamiento(Estancia pedida, boolean soloActivas) implements GuiaBusqueda<Reserva> {

        /// Coincide si la reserva se cruza con la estancia pedida (y está activa, si se pidió).
        @Override
        public boolean coincide(Reserva reserva) {
            return (!soloActivas || reserva.estaActiva()) && reserva.solapa(pedida);
        }

        /// Solo vale la pena ir a la derecha si este nodo empieza antes de la salida pedida.
        @Override
        public boolean explorarDerecha(Reserva reserva) {
            return reserva.obtenerEstancia().ingreso().isBefore(pedida.salida());
        }
    }
}
