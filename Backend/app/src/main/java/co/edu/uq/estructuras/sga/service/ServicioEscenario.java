package co.edu.uq.estructuras.sga.service;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.apartamentos.EstadoApartamento;
import co.edu.uq.estructuras.sga.core.huespedes.Huesped;
import co.edu.uq.estructuras.sga.core.reservas.SolicitudReserva;
import co.edu.uq.estructuras.sga.escenario.Escenario;
import co.edu.uq.estructuras.sga.escenario.InterpreteEscenario;
import co.edu.uq.estructuras.sga.repository.RepositorioApartamentos;
import co.edu.uq.estructuras.sga.repository.RepositorioEmpleados;
import co.edu.uq.estructuras.sga.repository.RepositorioHuespedes;
import co.edu.uq.estructuras.sga.repository.RepositorioReservas;
import co.edu.uq.estructuras.sga.service.Util.ApartamentoNecesitaPreparacionEvento;
import co.edu.uq.estructuras.sga.service.Util.EscenarioReiniciadoEvento;
import co.edu.uq.estructuras.sga.service.tiempo.ProveedorTiempo;


import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/// Carga un escenario de prueba (apartamentos, pasillos, huéspedes, reservas...) desde un archivo
/// en JSON. Cargar reemplaza todo lo que hay en memoria.
@Service
public class ServicioEscenario {

    /// Cantidad de elementos cargados de cada tipo.
    public record ResumenCarga(int apartamentos, int nodosMapa, int pasillos, int huespedes, int empleados,
                              int reservas, int solicitudesExternas, int periodosTemporada) {
    }

    private final InterpreteEscenario interprete;
    private final RepositorioApartamentos repositorioApartamentos;
    private final RepositorioHuespedes repositorioHuespedes;
    private final RepositorioEmpleados repositorioEmpleados;
    private final RepositorioReservas repositorioReservas;
    private final ServicioApartamentos apartamentos;
    private final ServicioHuespedes huespedes;
    private final ServicioEmpleados empleados;
    private final ServicioTemporadas temporadas;
    private final ServicioReservas reservas;
    private final ServicioMapaEdificio mapaEdificio;
    private final ServicioCanalExterno canalExterno;
    private final ServicioPreparacion preparacion;
    private final ProveedorTiempo tiempo;
    private final ApplicationEventPublisher eventos;

    /// Recibe por inyección el intérprete, los repositorios, los servicios a recargar y el publicador de eventos.
    public ServicioEscenario(InterpreteEscenario interprete, RepositorioApartamentos repositorioApartamentos,
                           RepositorioHuespedes repositorioHuespedes, RepositorioEmpleados repositorioEmpleados,
                           RepositorioReservas repositorioReservas, ServicioApartamentos apartamentos,
                           ServicioHuespedes huespedes, ServicioEmpleados empleados, ServicioTemporadas temporadas,
                           ServicioReservas reservas, ServicioMapaEdificio mapaEdificio,
                           ServicioCanalExterno canalExterno, ServicioPreparacion preparacion,
                           ProveedorTiempo tiempo, ApplicationEventPublisher eventos) {
        this.interprete = interprete;
        this.repositorioApartamentos = repositorioApartamentos;
        this.repositorioHuespedes = repositorioHuespedes;
        this.repositorioEmpleados = repositorioEmpleados;
        this.repositorioReservas = repositorioReservas;
        this.apartamentos = apartamentos;
        this.huespedes = huespedes;
        this.empleados = empleados;
        this.temporadas = temporadas;
        this.reservas = reservas;
        this.mapaEdificio = mapaEdificio;
        this.canalExterno = canalExterno;
        this.preparacion = preparacion;
        this.tiempo = tiempo;
        this.eventos = eventos;
    }

    /// Reemplaza todos los datos en memoria por los del escenario recibido.
    public ResumenCarga cargar(String contenido) {
        // Primero se interpreta: si el archivo es inválido, los datos actuales no se tocan.
        Escenario escenario = interprete.interpretar(contenido, tiempo.ahora());
        reinicio();

        temporadas.reemplazarPeriodos(escenario.temporadas());
        for (DatosApartamento apartamento : escenario.apartamentos()) {
            apartamentos.crear(apartamento);
        }
        for (var nodo : escenario.nodos()) {
            mapaEdificio.agregarNodo(nodo);
        }
        for (var pasillo : escenario.pasillos()) {
            mapaEdificio.agregarPasillo(pasillo.desde(), pasillo.hasta(), pasillo.metros());
            if (!pasillo.abierto()) {
                mapaEdificio.establecerAbiertoPasillo(pasillo.desde(), pasillo.hasta(), false);
            }
        }
        for (var huesped : escenario.huespedes()) {
            Huesped registrado = huespedes.registrar(huesped.perfil());
            for (String favorito : huesped.favoritos()) {
                registrado.agregarFavorito(favorito.toUpperCase());
            }
        }
        for (var empleado : escenario.empleados()) {
            empleados.crear(empleado.id(), empleado.nombreCompleto(), empleado.rol(), empleado.correo(), empleado.telefono());
        }
        for (var reserva : escenario.reservas()) {
            reservas.restaurar(new SolicitudReserva(reserva.codigoApartamento(), reserva.documentoTitular(),
                            reserva.ocupantes(), reserva.estancia(), reserva.canal(), reserva.llegada()),
                    reserva.estado(), reserva.pagado(), reserva.fechaCreacion());
        }
        for (var status : escenario.estadosApartamento()) {
            Apartamento apartamento = apartamentos.buscar(status.codigoApartamento());
            apartamento.restaurarEstado(status.estado(), status.motivo(), tiempo.hoy());
            if (status.estado() == EstadoApartamento.PENDIENTE_PREPARACION) {
                eventos.publishEvent(new ApartamentoNecesitaPreparacionEvento(apartamento.obtenerCodigo()));
            }
        }
        for (var registro : escenario.registrosPreparacion()) {
            preparacion.registrarHistorica(registro);
        }
        for (var solicitud : escenario.solicitudesExternas()) {
            canalExterno.recibir(solicitud.conId(canalExterno.siguienteIdSolicitud(), tiempo.ahora()));
        }
        return new ResumenCarga(escenario.apartamentos().tamano(), escenario.nodos().tamano(), escenario.pasillos().tamano(),
                escenario.huespedes().tamano(), escenario.empleados().tamano(), escenario.reservas().tamano(),
                escenario.solicitudesExternas().tamano(), escenario.temporadas().tamano());
    }

    /// Vacía todos los repositorios y avisa con [EscenarioReiniciadoEvento] para que las colas se limpien.
    private void reinicio() {
        repositorioApartamentos.vaciar();
        repositorioHuespedes.vaciar();
        repositorioEmpleados.vaciar();
        repositorioReservas.vaciar();
        temporadas.vaciar();
        eventos.publishEvent(new EscenarioReiniciadoEvento());
    }
}
