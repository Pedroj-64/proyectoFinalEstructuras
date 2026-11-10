package co.edu.uq.estructuras.sga.service;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.apartamentos.EstadoApartamento;
import co.edu.uq.estructuras.sga.core.folio.Folio;
import co.edu.uq.estructuras.sga.core.folio.TipoMovimientoFolio;
import co.edu.uq.estructuras.sga.core.huespedes.Huesped;
import co.edu.uq.estructuras.sga.core.huespedes.RegistroEstancia;
import co.edu.uq.estructuras.sga.core.reservas.AsignadorGrupos;
import co.edu.uq.estructuras.sga.core.reservas.CanalReserva;
import co.edu.uq.estructuras.sga.core.reservas.ComparadoresReserva;
import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.reservas.EstadoReserva;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.core.reservas.Ocupante;
import co.edu.uq.estructuras.sga.core.reservas.Reserva;
import co.edu.uq.estructuras.sga.core.reservas.SolicitudReserva;
import co.edu.uq.estructuras.sga.core.reservas.SolicitudReservaGrupo;
import co.edu.uq.estructuras.sga.core.tarifas.Cotizacion;
import co.edu.uq.estructuras.sga.core.tarifas.LiquidadorEstancia;
import co.edu.uq.estructuras.sga.core.tarifas.PoliticaCanal;
import co.edu.uq.estructuras.sga.core.tarifas.PoliticaReembolso;
import co.edu.uq.estructuras.sga.core.tarifas.PoliticasCanal;
import co.edu.uq.estructuras.sga.core.tarifas.VistaPreviaReembolso;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.CapacidadExcedidaExcepcion;
import co.edu.uq.estructuras.sga.excepciones.NoEncontradoExcepcion;
import co.edu.uq.estructuras.sga.excepciones.RangoFechasInvalidoExcepcion;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.excepciones.ReservaSolapadaExcepcion;
import co.edu.uq.estructuras.sga.repository.RepositorioApartamentos;
import co.edu.uq.estructuras.sga.repository.RepositorioHuespedes;
import co.edu.uq.estructuras.sga.repository.RepositorioReservas;
import co.edu.uq.estructuras.sga.service.Util.ApartamentoNecesitaPreparacionEvento;
import co.edu.uq.estructuras.sga.service.Util.CambioAgendaReservaEvento;
import co.edu.uq.estructuras.sga.service.tiempo.ProveedorTiempo;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/// Ciclo de vida de la reserva: creación desde cualquier canal, anticipo, cancelación, check-in,
/// check-out y no-show. Aquí vive toda regla que involucra más de una entidad (reserva, apartamento,
/// huésped y folio).
///
/// Cada vez que una reserva se crea o deja de estar activa publica un [CambioAgendaReservaEvento]
/// (patrón Observer), y al hacer check-out publica un [ApartamentoNecesitaPreparacionEvento].
@Service
public class ServicioReservas {

    /// Máximo de noches que puede durar una estancia.
    public static final int MAXIMO_NOCHES = 60;
    /// Hora de llegada que se usa cuando la solicitud no indica una.
    public static final LocalTime LLEGADA_POR_DEFECTO = LocalTime.of(15, 0);

    private final RepositorioReservas reservas;
    private final RepositorioApartamentos apartamentos;
    private final RepositorioHuespedes huespedes;
    private final LiquidadorEstancia liquidador;
    private final PoliticaReembolso politicaReembolso;
    private final AsignadorGrupos asignadorGrupos;
    private final ServicioDisponibilidad disponibilidad;
    private final ProveedorTiempo tiempo;
    private final ApplicationEventPublisher eventos;

    /// Recibe por inyección repositorios, liquidador, política de reembolso, asignador de grupos,
    /// servicio de disponibilidad, reloj y publicador de eventos.
    ///
    /// @param reservas repositorio de reservas
    /// @param apartamentos repositorio de apartamentos
    /// @param huespedes repositorio de huéspedes
    /// @param liquidador calcula el precio de una estancia noche por noche
    /// @param politicaReembolso decide qué porcentaje se devuelve al cancelar (Strategy)
    /// @param asignadorGrupos reparte grupos grandes en varios apartamentos (backtracking)
    /// @param disponibilidad busca apartamentos libres para una estancia
    /// @param tiempo reloj del sistema
    /// @param eventos publicador de eventos de Spring
    public ServicioReservas(RepositorioReservas reservas, RepositorioApartamentos apartamentos,
                              RepositorioHuespedes huespedes, LiquidadorEstancia liquidador, PoliticaReembolso politicaReembolso,
                              AsignadorGrupos asignadorGrupos, ServicioDisponibilidad disponibilidad,
                              ProveedorTiempo tiempo, ApplicationEventPublisher eventos) {
        this.reservas = reservas;
        this.apartamentos = apartamentos;
        this.huespedes = huespedes;
        this.liquidador = liquidador;
        this.politicaReembolso = politicaReembolso;
        this.asignadorGrupos = asignadorGrupos;
        this.disponibilidad = disponibilidad;
        this.tiempo = tiempo;
        this.eventos = eventos;
    }

    // ---------------------------------------------------------------- consultas

    /// Caso de uso: consultar una reserva por su id.
    ///
    /// @param id id de la reserva
    /// @return la reserva
    /// @throws NoEncontradoExcepcion si no existe una reserva con ese id
    public Reserva buscar(String id) {
        Reserva reserva = reservas.buscarPorId(id);
        if (reserva == null) {
            throw new NoEncontradoExcepcion("La reserva", id);
        }
        return reserva;
    }

    /// Caso de uso: listar reservas, filtradas por estado y canal (ambos opcionales), primero las más recientes.
    ///
    /// @param estado estado por el que se filtra, o `null` para no filtrar por estado
    /// @param canal canal por el que se filtra, o `null` para no filtrar por canal
    /// @return las reservas que cumplen los filtros, de la más reciente a la más antigua
    public ListaDoblementeEnlazada<Reserva> buscarTodos(EstadoReserva estado, CanalReserva canal) {
        return reservas.buscarTodos()
                .filtrar(r -> (estado == null || r.obtenerEstado() == estado) && (canal == null || r.obtenerCanal() == canal))
                .ordenada(ComparadoresReserva.porCreacionDescendente());
    }

    /// Caso de uso: ver la agenda de un apartamento.
    ///
    /// @param codigoApartamento código del apartamento
    /// @return las reservas del apartamento, en orden de fecha de entrada
    /// @throws NoEncontradoExcepcion si el apartamento no existe
    public ListaDoblementeEnlazada<Reserva> buscarPorApartamento(String codigoApartamento) {
        return reservas.buscarPorApartamento(exigirApartamento(codigoApartamento).obtenerCodigo());
    }

    /// Caso de uso: cotizar una estancia sin reservar. Valida la capacidad y liquida noche por noche.
    ///
    /// @param codigoApartamento código del apartamento
    /// @param estancia fechas de entrada y salida
    /// @param ocupantes personas que se alojarían
    /// @return la cotización con el desglose por noche y el total
    /// @throws NoEncontradoExcepcion si el apartamento no existe
    /// @throws ReglaNegocioExcepcion si no hay ocupantes
    /// @throws CapacidadExcedidaExcepcion si hay más ocupantes que la capacidad del apartamento
    public Cotizacion cotizacion(String codigoApartamento, Estancia estancia, ListaDoblementeEnlazada<Ocupante> ocupantes) {
        Apartamento apartamento = exigirApartamento(codigoApartamento);
        validarOcupantes(apartamento, ocupantes);
        return liquidador.cotizacion(apartamento, estancia, ocupantes);
    }

    // ---------------------------------------------------------------- creación

    /// Caso de uso: crear una reserva desde el portal, recepción o una plataforma externa. Aplica la política
    /// del canal (Strategy) y valida fechas, capacidad y solapamiento.
    ///
    /// @param solicitud datos de la reserva
    /// @return la reserva creada
    /// @throws RangoFechasInvalidoExcepcion si la entrada ya pasó o la estancia supera `MAXIMO_NOCHES`
    /// @throws NoEncontradoExcepcion si el apartamento o el huésped titular no existen
    /// @throws ReglaNegocioExcepcion si el apartamento está fuera de servicio
    /// @throws CapacidadExcedidaExcepcion si hay más ocupantes que la capacidad del apartamento
    /// @throws ReservaSolapadaExcepcion si el apartamento ya tiene una reserva activa en esas noches
    public Reserva crear(SolicitudReserva solicitud) {
        validarEstanciaFutura(solicitud.estancia());
        return reservar(solicitud, tiempo.ahora());
    }

    /// Caso de uso: reservar para un grupo grande. El [AsignadorGrupos] elige, con backtracking, la menor
    /// cantidad de apartamentos libres donde cabe el grupo; luego se reparten los ocupantes en orden y se
    /// crea una reserva por apartamento. Si una de las reservas falla, las anteriores ya quedaron creadas.
    ///
    /// @param solicitud datos del grupo
    /// @return una reserva por cada apartamento elegido
    /// @throws RangoFechasInvalidoExcepcion si la entrada ya pasó o la estancia supera `MAXIMO_NOCHES`
    /// @throws CapacidadExcedidaExcepcion si el grupo no cabe en los apartamentos disponibles
    public ListaDoblementeEnlazada<Reserva> crearParaGrupo(SolicitudReservaGrupo solicitud) {
        validarEstanciaFutura(solicitud.estancia());
        int tamanoGrupo = solicitud.ocupantes().tamano();
        ListaDoblementeEnlazada<Apartamento> elegidos = asignadorGrupos.asignar(
                disponibilidad.apartamentosDisponibles(solicitud.estancia(), 1), tamanoGrupo);

        ListaDoblementeEnlazada<Reserva> creada = new ListaDoblementeEnlazada<>();
        ListaDoblementeEnlazada<Ocupante> pendiente = ListaDoblementeEnlazada.copiarDe(solicitud.ocupantes());
        for (Apartamento apartamento : elegidos) {
            ListaDoblementeEnlazada<Ocupante> asignada = new ListaDoblementeEnlazada<>();
            while (!pendiente.estaVacia() && asignada.tamano() < apartamento.obtenerCapacidad()) {
                asignada.agregarUltimo(pendiente.quitarPrimero());
            }
            creada.agregarUltimo(reservar(new SolicitudReserva(apartamento.obtenerCodigo(), solicitud.documentoTitular(), asignada,
                    solicitud.estancia(), solicitud.canal(), solicitud.llegadaEstimada()), tiempo.ahora()));
        }
        return creada;
    }

    /// Caso de uso: restaurar una reserva desde un archivo de escenario. Las fechas pueden estar en el pasado,
    /// pero las reglas de solapamiento y capacidad se siguen aplicando. Una estancia finalizada siempre
    /// queda pagada por completo (el check-out lo exige) y ningún pago supera lo adeudado.
    ///
    /// @param solicitud datos de la reserva
    /// @param estadoDestino estado en el que debe quedar la reserva
    /// @param pagado dinero que el huésped ya había pagado
    /// @param fechaCreacion fecha y hora en que se creó originalmente
    /// @return la reserva restaurada
    /// @throws ReglaNegocioExcepcion si `estadoDestino` es PENDIENTE y la reserva nació confirmada; además
    ///         lanza las mismas excepciones que [#crear(SolicitudReserva)], salvo la de fecha de entrada pasada
    public Reserva restaurar(SolicitudReserva solicitud, EstadoReserva estadoDestino, Dinero pagado,
                               LocalDateTime fechaCreacion) {
        Reserva reserva = reservar(solicitud, fechaCreacion);
        Dinero adeudado = reserva.obtenerFolio().saldo();
        Dinero pago = estadoDestino == EstadoReserva.FINALIZADA || pagado.esMayorQue(adeudado) ? adeudado : pagado;
        if (pago.esPositivo()) {
            reserva.obtenerFolio().registrarMovimientoSistema(TipoMovimientoFolio.PAGO, pago, "Pago registrado en la carga inicial", fechaCreacion);
            reserva.confirmarSiAnticipoCubierto();
        }
        moverPor(reserva, estadoDestino);
        return reserva;
    }

    /// Lleva la reserva por los estados intermedios hasta `destino` de forma recursiva (por ejemplo, para
    /// llegar a FINALIZADA primero pasa por CONFIRMADA y EN_CURSO). Se usa al restaurar reservas del escenario.
    ///
    /// @param reserva reserva que se mueve
    /// @param destino estado al que debe llegar
    private void moverPor(Reserva reserva, EstadoReserva destino) {
        if (reserva.obtenerEstado() == destino) {
            return;
        }
        switch (destino) {
            case CONFIRMADA -> reserva.pasarA(EstadoReserva.CONFIRMADA);
            case EN_CURSO -> {
                moverPor(reserva, EstadoReserva.CONFIRMADA);
                reserva.pasarA(EstadoReserva.EN_CURSO);
            }
            case FINALIZADA -> {
                moverPor(reserva, EstadoReserva.EN_CURSO);
                reserva.pasarA(EstadoReserva.FINALIZADA);
                registrarHistorial(reserva);
            }
            case CANCELADA, NO_PRESENTADO -> {
                if (destino == EstadoReserva.NO_PRESENTADO) {
                    moverPor(reserva, EstadoReserva.CONFIRMADA);
                }
                reserva.pasarA(destino);
                asentarTerminacion(reserva, 0, reserva.obtenerFechaCreacion());
            }
            case PENDIENTE -> throw new ReglaNegocioExcepcion("Una reserva no puede volver a Pendiente");
        }
    }

    /// Núcleo de la creación: valida duración, estado del apartamento, capacidad y solapamiento; liquida la
    /// estancia; aplica la política del canal (estado inicial y vencimiento); carga el alojamiento al folio;
    /// guarda la reserva y publica el [CambioAgendaReservaEvento]. Si la solicitud no trae ocupantes, el
    /// titular se aloja solo.
    ///
    /// @param solicitud datos de la reserva
    /// @param fechaCreacion fecha y hora que se registran como creación
    /// @return la reserva creada
    private Reserva reservar(SolicitudReserva solicitud, LocalDateTime fechaCreacion) {
        Apartamento apartamento = exigirApartamento(solicitud.codigoApartamento());
        Huesped titular = exigirHuesped(solicitud.documentoTitular());
        Estancia estancia = solicitud.estancia();
        if (estancia.noches() > MAXIMO_NOCHES) {
            throw new RangoFechasInvalidoExcepcion("Una estancia no puede superar " + MAXIMO_NOCHES + " noches");
        }
        if (!apartamento.esReservable()) {
            throw new ReglaNegocioExcepcion("El apartamento " + apartamento.obtenerCodigo() + " está fuera de servicio: "
                    + apartamento.obtenerMotivoFueraDeServicio());
        }
        ListaDoblementeEnlazada<Ocupante> ocupantes = solicitud.ocupantes() == null || solicitud.ocupantes().estaVacia()
                ? ListaDoblementeEnlazada.de(titular.comoOcupante())
                : solicitud.ocupantes();
        validarOcupantes(apartamento, ocupantes);

        Reserva choque = reservas.buscarSolapada(apartamento.obtenerCodigo(), estancia);
        if (choque != null) {
            throw new ReservaSolapadaExcepcion(apartamento.obtenerCodigo(), estancia.toString(), choque.obtenerId());
        }

        PoliticaCanal politica = PoliticasCanal.paraCanal(solicitud.canal());
        LocalDateTime fechaVencimiento = politica.venceSinAnticipo()
                ? fechaCreacion.plusHours(Reserva.HORAS_PARA_VENCER_PENDIENTE)
                : null;
        Cotizacion cotizacion = liquidador.cotizacion(apartamento, estancia, ocupantes);
        Reserva reserva = new Reserva(reservas.siguienteId(), apartamento, titular, ocupantes, estancia,
                politica.canal(), politica.estadoInicial(), fechaCreacion, fechaVencimiento,
                solicitud.llegadaEstimada() == null ? LLEGADA_POR_DEFECTO : solicitud.llegadaEstimada(), cotizacion);
        reserva.obtenerFolio().registrarMovimientoSistema(TipoMovimientoFolio.CARGO, cotizacion.total(),
                "Alojamiento %d noche(s) en %s".formatted(estancia.noches(), apartamento.obtenerCodigo()), fechaCreacion);
        reservas.guardar(reserva);
        eventos.publishEvent(new CambioAgendaReservaEvento(reserva));
        return reserva;
    }

    // ---------------------------------------------------------------- anticipo y cancelación

    /// Caso de uso: consultar cuánto debe pagar el huésped como anticipo (el 30 % del total).
    ///
    /// @param id id de la reserva
    /// @return el anticipo exigido
    /// @throws NoEncontradoExcepcion si la reserva no existe
    public Dinero anticipoRequerido(String id) {
        return buscar(id).anticipoRequerido();
    }

    /// Caso de uso: ver cuánto se devolvería si la reserva se cancelara hoy, antes de confirmar la cancelación.
    ///
    /// @param id id de la reserva
    /// @return lo pagado, el porcentaje y el valor que se devolverían, la penalidad y los días que faltan
    /// @throws NoEncontradoExcepcion si la reserva no existe
    /// @throws ReglaNegocioExcepcion si la reserva no está pendiente ni confirmada
    public VistaPreviaReembolso vistaPreviaReembolso(String id) {
        Reserva reserva = buscar(id);
        if (!reserva.estaActiva() || reserva.obtenerEstado() == EstadoReserva.EN_CURSO) {
            throw new ReglaNegocioExcepcion("Solo se pueden cancelar reservas pendientes o confirmadas");
        }
        return vistaPreviaPara(reserva);
    }

    /// Calcula el reembolso que tendría la reserva si se cancela hoy, según la [PoliticaReembolso].
    ///
    /// @param reserva reserva que se cancelaría
    /// @return la vista previa del reembolso
    private VistaPreviaReembolso vistaPreviaPara(Reserva reserva) {
        Dinero pagado = reserva.obtenerFolio().montoPagado().maximo(Dinero.CERO);
        int porcentaje = politicaReembolso.porcentajeReembolso(tiempo.hoy(), reserva.obtenerEstancia().ingreso());
        Dinero reembolso = pagado.porcentaje(porcentaje);
        long diasAntes = ChronoUnit.DAYS.between(tiempo.hoy(), reserva.obtenerEstancia().ingreso());
        return new VistaPreviaReembolso(reserva.obtenerId(), pagado, porcentaje, reembolso, pagado.menos(reembolso), diasAntes);
    }

    /// Caso de uso: cancelar una reserva pendiente o confirmada. Devuelve lo que corresponde según la
    /// política de reembolso, cierra el folio y publica el [CambioAgendaReservaEvento].
    ///
    /// @param id id de la reserva
    /// @return la vista previa con la que se calculó la devolución
    /// @throws NoEncontradoExcepcion si la reserva no existe
    /// @throws ReglaNegocioExcepcion si la reserva no está pendiente ni confirmada
    public VistaPreviaReembolso cancelar(String id) {
        Reserva reserva = buscar(id);
        VistaPreviaReembolso vistaPrevia = vistaPreviaReembolso(id);
        reserva.pasarA(EstadoReserva.CANCELADA);
        asentarTerminacion(reserva, vistaPrevia.porcentaje(), tiempo.ahora());
        eventos.publishEvent(new CambioAgendaReservaEvento(reserva));
        return vistaPrevia;
    }

    /// Caso de uso: vencer una reserva pendiente que no pagó el anticipo a tiempo. La llama
    /// [ServicioVencimientos]; se cancela y se devuelve el 100 % de lo pagado.
    ///
    /// @param id id de la reserva
    /// @return `true` si la venció; `false` si ya no estaba pendiente (se confirmó o se canceló antes)
    /// @throws NoEncontradoExcepcion si la reserva no existe
    public boolean vencer(String id) {
        Reserva reserva = buscar(id);
        if (reserva.obtenerEstado() != EstadoReserva.PENDIENTE) {
            return false;
        }
        reserva.pasarA(EstadoReserva.CANCELADA);
        asentarTerminacion(reserva, 100, tiempo.ahora());
        eventos.publishEvent(new CambioAgendaReservaEvento(reserva));
        return true;
    }

    /// Cierra el folio de una reserva que no se usará: se compensa el cargo de alojamiento,
    /// el dinero no reembolsado queda como penalidad y el resto se devuelve.
    ///
    /// @param reserva reserva que termina
    /// @param porcentajeReembolso porcentaje de lo pagado que se devuelve, de 0 a 100
    /// @param en fecha y hora de los movimientos
    private void asentarTerminacion(Reserva reserva, int porcentajeReembolso, LocalDateTime en) {
        Folio folio = reserva.obtenerFolio();
        Dinero pagado = folio.montoPagado().maximo(Dinero.CERO);
        Dinero reembolso = pagado.porcentaje(porcentajeReembolso);
        Dinero penalidad = pagado.menos(reembolso);
        folio.registrarMovimientoSistema(TipoMovimientoFolio.ANULACION_CARGO, reserva.obtenerCotizacion().total(),
                "Anulación del alojamiento (" + reserva.obtenerEstado().nombreMostrado() + ")", en);
        if (penalidad.esPositivo()) {
            folio.registrarMovimientoSistema(TipoMovimientoFolio.CARGO, penalidad, "Penalidad por " + reserva.obtenerEstado().nombreMostrado().toLowerCase(), en);
        }
        if (reembolso.esPositivo()) {
            folio.registrarMovimientoSistema(TipoMovimientoFolio.REEMBOLSO, reembolso, "Devolución del " + porcentajeReembolso + " %", en);
        }
    }

    // ---------------------------------------------------------------- llegada y salida

    /// Caso de uso: check-in. Exige una reserva confirmada, que hoy sea una noche de la estancia y que el
    /// apartamento esté Preparado; el apartamento queda Ocupado y la reserva En curso.
    ///
    /// @param id id de la reserva
    /// @return la reserva en curso
    /// @throws NoEncontradoExcepcion si la reserva no existe
    /// @throws ReglaNegocioExcepcion si no está confirmada, si hoy no es una noche de la estancia o si el
    ///         apartamento no está Preparado
    public Reserva ingreso(String id) {
        Reserva reserva = buscar(id);
        if (reserva.obtenerEstado() != EstadoReserva.CONFIRMADA) {
            throw new ReglaNegocioExcepcion("Solo se puede hacer check-in de una reserva confirmada (estado actual: "
                    + reserva.obtenerEstado().nombreMostrado() + ")");
        }
        if (!reserva.obtenerEstancia().incluyeNoche(tiempo.hoy())) {
            throw new ReglaNegocioExcepcion("El check-in solo es posible durante la estancia ("
                    + reserva.obtenerEstancia() + ")");
        }
        Apartamento apartamento = reserva.obtenerApartamento();
        if (apartamento.obtenerEstado() != EstadoApartamento.PREPARADO) {
            throw new ReglaNegocioExcepcion("El apartamento " + apartamento.obtenerCodigo() + " no está Preparado (estado: "
                    + apartamento.obtenerEstado().nombreMostrado() + ")");
        }
        apartamento.cambiarEstado(EstadoApartamento.OCUPADO);
        reserva.pasarA(EstadoReserva.EN_CURSO);
        return reserva;
    }

    /// Caso de uso: check-out. Exige una reserva en curso con saldo en cero; la reserva queda Finalizada,
    /// se guarda en el historial del titular, el apartamento pasa a Pendiente de preparación y se publica
    /// un [ApartamentoNecesitaPreparacionEvento].
    ///
    /// @param id id de la reserva
    /// @return la reserva finalizada
    /// @throws NoEncontradoExcepcion si la reserva no existe
    /// @throws ReglaNegocioExcepcion si no está en curso o si el folio tiene saldo pendiente
    public Reserva salida(String id) {
        Reserva reserva = buscar(id);
        if (reserva.obtenerEstado() != EstadoReserva.EN_CURSO) {
            throw new ReglaNegocioExcepcion("Solo se puede hacer check-out de una reserva en curso");
        }
        Dinero saldo = reserva.obtenerFolio().saldo();
        if (saldo.esPositivo()) {
            throw new ReglaNegocioExcepcion("El folio tiene un saldo pendiente de " + saldo + ". Registre el pago antes del check-out");
        }
        reserva.pasarA(EstadoReserva.FINALIZADA);
        reserva.obtenerApartamento().cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION);
        registrarHistorial(reserva);
        eventos.publishEvent(new ApartamentoNecesitaPreparacionEvento(reserva.obtenerApartamento().obtenerCodigo()));
        return reserva;
    }

    /// Caso de uso: marcar como no presentado (no-show) a un huésped con reserva confirmada que no llegó.
    /// No se devuelve nada de lo pagado; se cierra el folio y se publica el [CambioAgendaReservaEvento].
    ///
    /// @param id id de la reserva
    /// @return la reserva marcada como no presentada
    /// @throws NoEncontradoExcepcion si la reserva no existe
    /// @throws ReglaNegocioExcepcion si no está confirmada o si todavía no llega la fecha de entrada
    public Reserva marcarNoPresentado(String id) {
        Reserva reserva = buscar(id);
        if (reserva.obtenerEstado() != EstadoReserva.CONFIRMADA) {
            throw new ReglaNegocioExcepcion("Solo una reserva confirmada puede marcarse como No-show");
        }
        if (tiempo.hoy().isBefore(reserva.obtenerEstancia().ingreso())) {
            throw new ReglaNegocioExcepcion("Aún no llega la fecha de entrada de la reserva");
        }
        reserva.pasarA(EstadoReserva.NO_PRESENTADO);
        asentarTerminacion(reserva, 0, tiempo.ahora());
        eventos.publishEvent(new CambioAgendaReservaEvento(reserva));
        return reserva;
    }

    // ---------------------------------------------------------------- utilidades

    /// Agrega la estancia terminada al historial del titular (lista doblemente enlazada del huésped).
    ///
    /// @param reserva reserva finalizada
    private void registrarHistorial(Reserva reserva) {
        reserva.obtenerTitular().registrarEstancia(new RegistroEstancia(reserva.obtenerId(), reserva.obtenerApartamento().obtenerCodigo(),
                reserva.obtenerApartamento().obtenerNombre(), reserva.obtenerEstancia(), reserva.cantidadOcupantes(),
                reserva.obtenerFolio().montoCargado()));
    }

    /// Rechaza estancias cuya entrada ya pasó según el reloj del sistema.
    ///
    /// @param estancia estancia que se valida
    /// @throws RangoFechasInvalidoExcepcion si la fecha de entrada es anterior a hoy
    private void validarEstanciaFutura(Estancia estancia) {
        if (estancia.ingreso().isBefore(tiempo.hoy())) {
            throw new RangoFechasInvalidoExcepcion("La fecha de entrada (" + estancia.ingreso() + ") ya pasó");
        }
    }

    /// Exige al menos un ocupante y no más que la capacidad del apartamento.
    ///
    /// @param apartamento apartamento que se reserva
    /// @param ocupantes personas que se alojarían
    /// @throws ReglaNegocioExcepcion si no hay ocupantes
    /// @throws CapacidadExcedidaExcepcion si hay más ocupantes que la capacidad
    private void validarOcupantes(Apartamento apartamento, ListaDoblementeEnlazada<Ocupante> ocupantes) {
        if (ocupantes.estaVacia()) {
            throw new ReglaNegocioExcepcion("La reserva debe tener al menos un ocupante");
        }
        if (ocupantes.tamano() > apartamento.obtenerCapacidad()) {
            throw new CapacidadExcedidaExcepcion("El apartamento " + apartamento.obtenerCodigo(), ocupantes.tamano(),
                    apartamento.obtenerCapacidad());
        }
    }

    /// Busca el apartamento por código.
    ///
    /// @param codigo código del apartamento
    /// @return el apartamento
    /// @throws NoEncontradoExcepcion si no existe
    private Apartamento exigirApartamento(String codigo) {
        Apartamento apartamento = apartamentos.buscarPorCodigo(codigo);
        if (apartamento == null) {
            throw new NoEncontradoExcepcion("El apartamento", codigo);
        }
        return apartamento;
    }

    /// Busca el huésped por documento.
    ///
    /// @param documento documento del huésped
    /// @return el huésped
    /// @throws NoEncontradoExcepcion si no existe
    private Huesped exigirHuesped(String documento) {
        Huesped huesped = huespedes.buscarPorDocumento(documento);
        if (huesped == null) {
            throw new NoEncontradoExcepcion("El huésped", documento);
        }
        return huesped;
    }
}
