package co.edu.uq.estructuras.sga.service;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.apartamentos.EstadoApartamento;
import co.edu.uq.estructuras.sga.core.tarifas.TarifasTemporada;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.NoEncontradoExcepcion;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.repository.RepositorioApartamentos;
import co.edu.uq.estructuras.sga.repository.RepositorioReservas;
import co.edu.uq.estructuras.sga.service.Util.ApartamentoNecesitaPreparacionEvento;
import co.edu.uq.estructuras.sga.service.tiempo.ProveedorTiempo;


import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/// Casos de uso del catálogo de apartamentos (administrador) y de su estado operativo.
@Service
public class ServicioApartamentos {

    private final RepositorioApartamentos apartamentos;
    private final RepositorioReservas reservas;
    private final ProveedorTiempo tiempo;
    private final ApplicationEventPublisher eventos;

    /// Recibe por inyección los repositorios y el publicador de eventos.
    public ServicioApartamentos(RepositorioApartamentos apartamentos, RepositorioReservas reservas,
                            ProveedorTiempo tiempo, ApplicationEventPublisher eventos) {
        this.apartamentos = apartamentos;
        this.reservas = reservas;
        this.tiempo = tiempo;
        this.eventos = eventos;
    }

    /// Todos los apartamentos en orden de código (recorrido inorden del árbol).
    public ListaDoblementeEnlazada<Apartamento> buscarTodos() {
        return apartamentos.buscarTodos();
    }

    /// Busca un apartamento por código o lanza [NoEncontradoExcepcion].
    public Apartamento buscar(String codigo) {
        Apartamento apartamento = apartamentos.buscarPorCodigo(codigo);
        if (apartamento == null) {
            throw new NoEncontradoExcepcion("El apartamento", codigo);
        }
        return apartamento;
    }

    /// Crea un apartamento; el código no se puede repetir.
    public Apartamento crear(DatosApartamento datos) {
        Apartamento apartamento = new Apartamento(datos.codigo(), datos.nombre(), datos.bloque(), datos.piso(),
                datos.habitaciones(), datos.capacidad(), datos.caracteristicas(), datos.tarifas());
        if (!apartamentos.guardar(apartamento)) {
            throw new ReglaNegocioExcepcion("Ya existe un apartamento con el código " + apartamento.obtenerCodigo());
        }
        return apartamento;
    }

    /// Actualiza los datos descriptivos de un apartamento.
    public Apartamento actualizar(String codigo, DatosApartamento datos) {
        Apartamento apartamento = buscar(codigo);
        apartamento.actualizarDetalles(datos.nombre(), datos.bloque(), datos.piso(), datos.habitaciones(), datos.capacidad(),
                datos.caracteristicas());
        if (datos.tarifas() != null) {
            apartamento.actualizarTarifas(datos.tarifas());
        }
        return apartamento;
    }

    /// Cambia las tarifas por persona de las tres temporadas.
    public Apartamento actualizarTarifas(String codigo, TarifasTemporada tarifas) {
        Apartamento apartamento = buscar(codigo);
        apartamento.actualizarTarifas(tarifas);
        return apartamento;
    }

    /// Elimina un apartamento que no tenga reservas activas.
    public void eliminar(String codigo) {
        Apartamento apartamento = buscar(codigo);
        boolean tieneActiva = reservas.buscarPorApartamento(apartamento.obtenerCodigo()).matchAny(r -> r.estaActiva());
        if (tieneActiva) {
            throw new ReglaNegocioExcepcion("No se puede eliminar " + apartamento.obtenerCodigo() + ": tiene reservas activas");
        }
        apartamentos.eliminar(apartamento.obtenerCodigo());
    }

    /// Cambio de estado manual hecho por el personal. El check-in, el check-out y la preparación
    /// tienen sus propios casos de uso; este cubre sacar un apartamento de servicio y reactivarlo.
    public Apartamento cambiarEstado(String codigo, EstadoApartamento destino, String motivo) {
        Apartamento apartamento = buscar(codigo);
        if (destino == EstadoApartamento.OCUPADO) {
            throw new ReglaNegocioExcepcion("Un apartamento solo pasa a Ocupado con el check-in de una reserva");
        }
        if (destino == EstadoApartamento.FUERA_DE_SERVICIO) {
            apartamento.marcarFueraDeServicio(motivo, tiempo.hoy());
        } else {
            apartamento.cambiarEstado(destino);
        }
        if (destino == EstadoApartamento.PENDIENTE_PREPARACION) {
            eventos.publishEvent(new ApartamentoNecesitaPreparacionEvento(apartamento.obtenerCodigo()));
        }
        return apartamento;
    }
}
