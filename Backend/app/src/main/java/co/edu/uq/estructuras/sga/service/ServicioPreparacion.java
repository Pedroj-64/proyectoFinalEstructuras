package co.edu.uq.estructuras.sga.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import co.edu.uq.estructuras.sga.estructuras.ColaPrioridadMonticulo;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.estructuras.MapaHash;
import co.edu.uq.estructuras.sga.excepciones.NoEncontradoExcepcion;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.core.preparacion.ComparadorTareaPreparacion;
import co.edu.uq.estructuras.sga.core.preparacion.PreparacionEnCurso;
import co.edu.uq.estructuras.sga.core.preparacion.RegistroPreparacion;
import co.edu.uq.estructuras.sga.core.preparacion.TareaPreparacion;
import co.edu.uq.estructuras.sga.core.reservas.Reserva;
import co.edu.uq.estructuras.sga.repository.RepositorioReservas;
import co.edu.uq.estructuras.sga.service.Util.ApartamentoNecesitaPreparacionEvento;
import co.edu.uq.estructuras.sga.service.Util.EscenarioReiniciadoEvento;

/// Cola de preparación de apartamentos (montículo de prioridad) y control de los que se están
/// preparando. No cambia el estado del apartamento: eso lo hace quien llama.
@Service
public class ServicioPreparacion {

    private final ColaPrioridadMonticulo<TareaPreparacion> cola =
            new ColaPrioridadMonticulo<>(ComparadorTareaPreparacion.INSTANCIA);
    private final MapaHash<String, PreparacionEnCurso> enCurso = new MapaHash<>();
    private int terminadas = 0;
    private long minutosAcumulados = 0;
    private final RepositorioReservas reservas;
    private final ServicioReloj reloj;

    /// Sin reservas ni reloj (pruebas): el listener de eventos no se puede usar.
    public ServicioPreparacion() {
        this(null, null);
    }

    @Autowired
    public ServicioPreparacion(RepositorioReservas reservas, ServicioReloj reloj) {
        this.reservas = reservas;
        this.reloj = reloj;
    }

    /// Observer: un apartamento quedó pendiente de preparación (check-out). Se encola con prioridad 1
    /// si su próxima reserva activa llega hoy, y con la hora estimada de esa llegada.
    @EventListener
    public void alNecesitarPreparacion(ApartamentoNecesitaPreparacionEvento evento) {
        LocalDateTime ahora = reloj.ahora();
        LocalDate hoy = ahora.toLocalDate();
        Reserva proxima = null;
        for (Reserva r : reservas.buscarPorApartamento(evento.codigoApartamento())) {
            if (r.estaActiva() && !r.obtenerEstancia().ingreso().isBefore(hoy)) {
                proxima = r; // ordenadas por entrada: la primera es la más cercana
                break;
            }
        }
        boolean llegadaHoy = proxima != null && proxima.obtenerEstancia().ingreso().equals(hoy);
        encolar(new TareaPreparacion(evento.codigoApartamento(), llegadaHoy,
                llegadaHoy ? proxima.obtenerLlegadaEstimada() : null, ahora));
    }

    /// Observer: vacía todo antes de cargar un escenario nuevo.
    @EventListener
    public void alReiniciarEscenario(EscenarioReiniciadoEvento evento) {
        cola.vaciar();
        enCurso.vaciar();
        terminadas = 0;
        minutosAcumulados = 0;
    }

    /// Suma una preparación ya terminada (por ejemplo, las del escenario de demostración) al promedio de tiempos.
    ///
    /// @param registro preparación terminada
    public void registrarHistorica(RegistroPreparacion registro) {
        terminadas++;
        minutosAcumulados += registro.minutos();
    }

    /// Agrega un apartamento a la cola; si ya estaba en cola o en preparación se rechaza.
    public void encolar(TareaPreparacion tarea) {
        if (estaEnCola(tarea.codigoApartamento()) || enCurso.contieneLlave(tarea.codigoApartamento())) {
            throw new ReglaNegocioExcepcion("El apartamento " + tarea.codigoApartamento() + " ya está en la cola de preparación");
        }
        cola.ofrecer(tarea);
    }

    /// Saca la tarea más urgente y la marca en preparación desde `ahora`.
    public PreparacionEnCurso iniciarSiguiente(LocalDateTime ahora) {
        if (cola.estaVacia()) {
            throw new ReglaNegocioExcepcion("No hay apartamentos en la cola de preparación");
        }
        TareaPreparacion tarea = cola.sacar();
        PreparacionEnCurso preparacion = new PreparacionEnCurso(tarea.codigoApartamento(), ahora);
        enCurso.poner(tarea.codigoApartamento(), preparacion);
        return preparacion;
    }

    /// Termina la preparación y acumula su duración para las estadísticas.
    public void terminar(String codigoApartamento, LocalDateTime ahora) {
        PreparacionEnCurso preparacion = enCurso.quitar(codigoApartamento);
        if (preparacion == null) {
            throw new NoEncontradoExcepcion("La preparación en curso", codigoApartamento);
        }
        terminadas++;
        minutosAcumulados += java.time.Duration.between(preparacion.fechaInicio(), ahora).toMinutes();
    }

    /// Quita un apartamento de la cola (por ejemplo, si pasa a fuera de servicio).
    public boolean cancelar(String codigoApartamento) {
        return cola.quitarSi(t -> t.codigoApartamento().equals(codigoApartamento)) > 0;
    }

    /// Cola en orden de atención, sin sacar nada.
    public ListaDoblementeEnlazada<TareaPreparacion> colaOrdenada() {
        return cola.aListaOrdenada();
    }

    /// Apartamentos que se están preparando ahora.
    public ListaDoblementeEnlazada<PreparacionEnCurso> enCurso() {
        return enCurso.valores();
    }

    /// Grupos de códigos por prioridad (1 primero), listos para `ServicioMapaEdificio.rutaPor`.
    public ListaDoblementeEnlazada<ListaDoblementeEnlazada<String>> gruposPorPrioridad() {
        ListaDoblementeEnlazada<String> hoy = new ListaDoblementeEnlazada<>();
        ListaDoblementeEnlazada<String> resto = new ListaDoblementeEnlazada<>();
        for (TareaPreparacion tarea : colaOrdenada()) {
            (tarea.prioridad() == 1 ? hoy : resto).agregarUltimo(tarea.codigoApartamento());
        }
        return ListaDoblementeEnlazada.de(hoy, resto);
    }

    public int preparacionesTerminadas() {
        return terminadas;
    }

    public int preparacionesEnCola() {
        return cola.tamano();
    }

    /// Minutos promedio por preparación terminada (0 si no hay ninguna).
    public double minutosPromedio() {
        return terminadas == 0 ? 0 : (double) minutosAcumulados / terminadas;
    }

    private boolean estaEnCola(String codigo) {
        return cola.matchAny(t -> t.codigoApartamento().equals(codigo));
    }
}
