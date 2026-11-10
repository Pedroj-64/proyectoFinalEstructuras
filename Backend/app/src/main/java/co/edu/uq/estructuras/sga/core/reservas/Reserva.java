package co.edu.uq.estructuras.sga.core.reservas;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.folio.Folio;
import co.edu.uq.estructuras.sga.core.huespedes.Huesped;
import co.edu.uq.estructuras.sga.core.tarifas.Cotizacion;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.TransicionEstadoInvalidaExcepcion;

import java.time.LocalDateTime;
import java.time.LocalTime;

/// Reserva de un apartamento completo. Orden natural: por fecha de entrada (y luego por id, para
/// que dos reservas que empiezan el mismo día puedan convivir en el árbol binario de búsqueda).
public class Reserva implements Comparable<Reserva> {

    /// Porcentaje del total de la estancia que se exige como anticipo.
    public static final int PORCENTAJE_ANTICIPO = 30;
    /// Plazo, en horas, para pagar el anticipo de una reserva pendiente. Esta clase no lo aplica:
    /// recibe la `fechaVencimiento` ya calculada en el constructor.
    public static final int HORAS_PARA_VENCER_PENDIENTE = 24;

    private final String id;
    private final Apartamento apartamento;
    private final Huesped titular;
    private final ListaDoblementeEnlazada<Ocupante> ocupantes;
    private final Estancia estancia;
    private final CanalReserva canal;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaVencimiento;
    private final LocalTime llegadaEstimada;
    private final Cotizacion cotizacion;
    private final Folio folio;
    private EstadoReserva estado;

    /// Crea la reserva en `estadoInicial` y le abre un folio vacío con número `"F-" + id`.
    ///
    /// Esta clase no valida fechas, capacidad ni solapamiento: quien la crea debe hacerlo antes.
    ///
    /// @param id identificador único de la reserva
    /// @param apartamento apartamento reservado
    /// @param titular huésped titular de la reserva
    /// @param ocupantes personas que se alojan
    /// @param estancia fechas de entrada y salida
    /// @param canal canal por el que llegó la reserva
    /// @param estadoInicial estado con el que nace, por ejemplo el que indica la política del canal
    /// @param fechaCreacion fecha y hora en que se creó
    /// @param fechaVencimiento hora límite para pagar el anticipo, o `null` si no vence
    /// @param llegadaEstimada hora estimada de llegada
    /// @param cotizacion precio de la estancia, noche por noche
    public Reserva(String id, Apartamento apartamento, Huesped titular, ListaDoblementeEnlazada<Ocupante> ocupantes,
                       Estancia estancia, CanalReserva canal, EstadoReserva estadoInicial,
                       LocalDateTime fechaCreacion, LocalDateTime fechaVencimiento, LocalTime llegadaEstimada, Cotizacion cotizacion) {
        this.id = id;
        this.apartamento = apartamento;
        this.titular = titular;
        this.ocupantes = ocupantes;
        this.estancia = estancia;
        this.canal = canal;
        this.estado = estadoInicial;
        this.fechaCreacion = fechaCreacion;
        this.fechaVencimiento = fechaVencimiento;
        this.llegadaEstimada = llegadaEstimada;
        this.cotizacion = cotizacion;
        this.folio = new Folio("F-" + id);
    }

    /// Cambia de estado validando el ciclo de vida de [EstadoReserva].
    ///
    /// @param destino estado al que se quiere pasar
    /// @throws TransicionEstadoInvalidaExcepcion si el ciclo de vida no permite pasar del estado actual a `destino`
    public void pasarA(EstadoReserva destino) {
        if (!estado.puedePasarA(destino)) {
            throw new TransicionEstadoInvalidaExcepcion("La reserva " + id, estado.nombreMostrado(), destino.nombreMostrado());
        }
        estado = destino;
    }

    /// Anticipo exigido: el `PORCENTAJE_ANTICIPO` (30 %) del total de la cotización, redondeado al peso.
    ///
    /// @return valor del anticipo
    public Dinero anticipoRequerido() {
        return cotizacion.total().porcentaje(PORCENTAJE_ANTICIPO);
    }

    /// Si la reserva está PENDIENTE y lo pagado en el folio es igual o mayor que el anticipo, la pasa a CONFIRMADA.
    ///
    /// @return `true` si la confirmó; `false` si no estaba pendiente o lo pagado no alcanza
    public boolean confirmarSiAnticipoCubierto() {
        if (estado == EstadoReserva.PENDIENTE && !anticipoRequerido().esMayorQue(folio.montoPagado())) {
            pasarA(EstadoReserva.CONFIRMADA);
            return true;
        }
        return false;
    }

    /// Indica si la reserva está activa.
    ///
    /// @return `true` si su estado es PENDIENTE, CONFIRMADA o EN_CURSO
    public boolean estaActiva() {
        return estado.estaActiva();
    }

    /// Indica si esta reserva comparte al menos una noche con la estancia dada.
    ///
    /// @param otro estancia con la que se compara
    /// @return `true` si comparten al menos una noche
    public boolean solapa(Estancia otro) {
        return estancia.solapa(otro);
    }

    /// Cantidad de ocupantes.
    ///
    /// @return número de personas en la lista de ocupantes
    public int cantidadOcupantes() {
        return ocupantes.tamano();
    }

    /// Identificador único.
    ///
    /// @return id de la reserva
    public String obtenerId() {
        return id;
    }

    /// Apartamento reservado.
    ///
    /// @return el apartamento de la reserva
    public Apartamento obtenerApartamento() {
        return apartamento;
    }

    /// Huésped titular.
    ///
    /// @return el titular de la reserva
    public Huesped obtenerTitular() {
        return titular;
    }

    /// Personas que se alojan.
    ///
    /// @return una copia de la lista de ocupantes; modificarla no altera la reserva
    public ListaDoblementeEnlazada<Ocupante> obtenerOcupantes() {
        return ListaDoblementeEnlazada.copiarDe(ocupantes);
    }

    /// Fechas de entrada y salida.
    ///
    /// @return la estancia de la reserva
    public Estancia obtenerEstancia() {
        return estancia;
    }

    /// Canal por el que llegó.
    ///
    /// @return canal de la reserva
    public CanalReserva obtenerCanal() {
        return canal;
    }

    /// Estado actual del ciclo de vida.
    ///
    /// @return estado actual
    public EstadoReserva obtenerEstado() {
        return estado;
    }

    /// Fecha y hora de creación.
    ///
    /// @return fecha y hora en que se creó la reserva
    public LocalDateTime obtenerFechaCreacion() {
        return fechaCreacion;
    }

    /// Hora límite para pagar el anticipo.
    ///
    /// @return la fecha de vencimiento recibida al crear la reserva, o `null` si no vence
    public LocalDateTime obtenerFechaVencimiento() {
        return fechaVencimiento;
    }

    /// Hora estimada de llegada.
    ///
    /// @return la hora recibida al crear la reserva; esta clase no le pone un valor por defecto
    public LocalTime obtenerLlegadaEstimada() {
        return llegadaEstimada;
    }

    /// Liquidación noche por noche.
    ///
    /// @return la cotización de la reserva
    public Cotizacion obtenerCotizacion() {
        return cotizacion;
    }

    /// Cuenta de la reserva.
    ///
    /// @return el folio de la reserva, con número `"F-" + id`
    public Folio obtenerFolio() {
        return folio;
    }

    /// Orden natural: por fecha de entrada y luego por id.
    ///
    /// @param otro reserva con la que se compara
    /// @return negativo, cero o positivo si esta reserva va antes, en el mismo lugar o después de `otro`
    @Override
    public int compareTo(Reserva otro) {
        int porIngreso = estancia.ingreso().compareTo(otro.estancia.ingreso());
        return porIngreso != 0 ? porIngreso : id.compareTo(otro.id);
    }

    /// Dos reservas son iguales si tienen el mismo id.
    ///
    /// @param otro objeto con el que se compara
    /// @return `true` si `otro` es una reserva con el mismo id
    @Override
    public boolean equals(Object otro) {
        return otro instanceof Reserva reserva && id.equals(reserva.id);
    }

    /// Consistente con [#equals(Object)]: usa el id.
    ///
    /// @return código hash del id
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
