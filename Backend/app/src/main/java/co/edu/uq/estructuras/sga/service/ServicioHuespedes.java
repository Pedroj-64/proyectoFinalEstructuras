package co.edu.uq.estructuras.sga.service;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.huespedes.Huesped;
import co.edu.uq.estructuras.sga.core.huespedes.PerfilHuesped;
import co.edu.uq.estructuras.sga.core.huespedes.RegistroEstancia;
import co.edu.uq.estructuras.sga.core.reservas.ComparadoresReserva;
import co.edu.uq.estructuras.sga.core.reservas.Reserva;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.NoEncontradoExcepcion;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.repository.RepositorioApartamentos;
import co.edu.uq.estructuras.sga.repository.RepositorioHuespedes;
import co.edu.uq.estructuras.sga.repository.RepositorioReservas;


import org.springframework.stereotype.Service;

/// Perfiles de huéspedes, favoritos e historial.
@Service
public class ServicioHuespedes {

    private final RepositorioHuespedes huespedes;
    private final RepositorioApartamentos apartamentos;
    private final RepositorioReservas reservas;

    /// Recibe por inyección los repositorios de huéspedes, apartamentos y reservas.
    public ServicioHuespedes(RepositorioHuespedes huespedes, RepositorioApartamentos apartamentos, RepositorioReservas reservas) {
        this.huespedes = huespedes;
        this.apartamentos = apartamentos;
        this.reservas = reservas;
    }

    /// Todos los huéspedes en orden de documento.
    public ListaDoblementeEnlazada<Huesped> buscarTodos() {
        return huespedes.buscarTodos();
    }

    /// Busca un huésped o lanza [NoEncontradoExcepcion].
    public Huesped buscar(String documento) {
        Huesped huesped = huespedes.buscarPorDocumento(documento);
        if (huesped == null) {
            throw new NoEncontradoExcepcion("El huésped", documento);
        }
        return huesped;
    }

    /// Registra un huésped nuevo; el documento no se puede repetir.
    public Huesped registrar(PerfilHuesped perfil) {
        Huesped huesped = new Huesped(perfil);
        if (!huespedes.guardar(huesped)) {
            throw new ReglaNegocioExcepcion("Ya existe un huésped con el documento " + huesped.obtenerDocumento());
        }
        return huesped;
    }

    /// Devuelve el huésped existente o lo registra (lo usa el canal externo).
    public Huesped buscarORegistrar(PerfilHuesped perfil) {
        Huesped existente = huespedes.buscarPorDocumento(perfil.documento());
        return existente != null ? existente : registrar(perfil);
    }

    /// Actualiza el perfil de un huésped.
    public Huesped actualizar(String documento, PerfilHuesped perfil) {
        Huesped huesped = buscar(documento);
        huesped.actualizar(perfil);
        return huesped;
    }

    /// Elimina un huésped sin reservas activas.
    public void eliminar(String documento) {
        Huesped huesped = buscar(documento);
        if (reservasDe(huesped.obtenerDocumento()).matchAny(Reserva::estaActiva)) {
            throw new ReglaNegocioExcepcion("No se puede eliminar el huésped: tiene reservas activas");
        }
        huespedes.eliminar(huesped.obtenerDocumento());
    }

    /// Agrega un favorito. Devuelve `false` si ya estaba guardado (sin repetidos).
    public boolean agregarFavorito(String documento, String codigoApartamento) {
        Huesped huesped = buscar(documento);
        Apartamento apartamento = apartamentos.buscarPorCodigo(codigoApartamento);
        if (apartamento == null) {
            throw new NoEncontradoExcepcion("El apartamento", codigoApartamento);
        }
        return huesped.agregarFavorito(apartamento.obtenerCodigo());
    }

    /// Quita un favorito. Devuelve si estaba guardado.
    public boolean quitarFavorito(String documento, String codigoApartamento) {
        return buscar(documento).quitarFavorito(codigoApartamento.trim().toUpperCase());
    }

    /// Apartamentos favoritos del huésped.
    public ListaDoblementeEnlazada<Apartamento> favoritos(String documento) {
        ListaDoblementeEnlazada<Apartamento> favoritos = new ListaDoblementeEnlazada<>();
        for (String codigo : buscar(documento).obtenerCodigosApartamentosFavoritos()) {
            Apartamento apartamento = apartamentos.buscarPorCodigo(codigo);
            if (apartamento != null) {
                favoritos.agregarUltimo(apartamento);
            }
        }
        return favoritos.ordenada((a, b) -> a.compareTo(b));
    }

    /// Historial de estancias finalizadas del huésped.
    public ListaDoblementeEnlazada<RegistroEstancia> historial(String documento) {
        return ListaDoblementeEnlazada.copiarDe(buscar(documento).obtenerHistorialEstancias());
    }

    /// Reservas en las que el huésped es titular.
    public ListaDoblementeEnlazada<Reserva> reservasDe(String documento) {
        return reservas.buscarTodos()
                .filtrar(reserva -> reserva.obtenerTitular().obtenerDocumento().equals(documento))
                .ordenada(ComparadoresReserva.porCreacionDescendente());
    }
}
