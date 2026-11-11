package co.edu.uq.estructuras.sga.escenario;

import co.edu.uq.estructuras.sga.core.apartamentos.EstadoApartamento;
import co.edu.uq.estructuras.sga.core.canal.SolicitudReservaExterna;
import co.edu.uq.estructuras.sga.core.edificio.NodoEdificio;
import co.edu.uq.estructuras.sga.core.huespedes.PerfilHuesped;
import co.edu.uq.estructuras.sga.core.personal.RolEmpleado;
import co.edu.uq.estructuras.sga.core.preparacion.RegistroPreparacion;
import co.edu.uq.estructuras.sga.core.reservas.CanalReserva;
import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.reservas.EstadoReserva;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.core.reservas.Ocupante;
import co.edu.uq.estructuras.sga.core.tarifas.PeriodoTemporada;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.service.DatosApartamento;


import java.time.LocalDateTime;
import java.time.LocalTime;

/// Todo lo leído de un archivo de escenario, ya convertido a valores del dominio.
public record Escenario(ListaDoblementeEnlazada<PeriodoTemporada> temporadas,
                       ListaDoblementeEnlazada<DatosApartamento> apartamentos,
                       ListaDoblementeEnlazada<LineaEstadoApartamento> estadosApartamento,
                       ListaDoblementeEnlazada<NodoEdificio> nodos,
                       ListaDoblementeEnlazada<LineaPasillo> pasillos,
                       ListaDoblementeEnlazada<LineaHuesped> huespedes,
                       ListaDoblementeEnlazada<LineaEmpleado> empleados,
                       ListaDoblementeEnlazada<LineaReserva> reservas,
                       ListaDoblementeEnlazada<RegistroPreparacion> registrosPreparacion,
                       ListaDoblementeEnlazada<LineaSolicitudExterna> solicitudesExternas) {

    /// Estado inicial de un apartamento leído del archivo.
    public record LineaEstadoApartamento(String codigoApartamento, EstadoApartamento estado, String motivo) {
    }

    /// Pasillo leído del archivo.
    public record LineaPasillo(String desde, String hasta, double metros, boolean abierto) {
    }

    /// Huésped leído del archivo con sus favoritos.
    public record LineaHuesped(PerfilHuesped perfil, String[] favoritos) {
    }

    /// Empleado leído del archivo.
    public record LineaEmpleado(String id, String nombreCompleto, RolEmpleado rol, String correo, String telefono) {
    }

    /// Reserva leída del archivo con su estado y lo pagado.
    public record LineaReserva(String codigoApartamento, String documentoTitular, ListaDoblementeEnlazada<Ocupante> ocupantes,
                                  Estancia estancia, CanalReserva canal, EstadoReserva estado, LocalTime llegada,
                                  Dinero pagado, LocalDateTime fechaCreacion) {
    }

    /// Solicitud externa que queda en la cola al cargar.
    public record LineaSolicitudExterna(String plataforma, String codigoApartamento, Estancia estancia, PerfilHuesped titular,
                                      ListaDoblementeEnlazada<Ocupante> ocupantes) {

        /// Convierte la línea del archivo en una solicitud real asignándole id y hora de llegada a la cola.
        public SolicitudReservaExterna conId(String idSolicitud, LocalDateTime fechaRecepcion) {
            return new SolicitudReservaExterna(idSolicitud, plataforma, codigoApartamento, estancia, titular, ocupantes, fechaRecepcion);
        }
    }
}
