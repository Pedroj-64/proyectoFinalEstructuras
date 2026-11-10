package co.edu.uq.estructuras.sga.api.dto;



import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import co.edu.uq.estructuras.sga.api.dto.DtosApartamento.ApartamentoDto;
import co.edu.uq.estructuras.sga.api.dto.DtosApartamento.TarifasDto;
import co.edu.uq.estructuras.sga.api.dto.DtosFolio.FolioDto;
import co.edu.uq.estructuras.sga.api.dto.DtosFolio.MovimientoFolioDto;
import co.edu.uq.estructuras.sga.api.dto.DtosHuesped.HuespedDto;
import co.edu.uq.estructuras.sga.api.dto.DtosHuesped.RegistroEstanciaDto;
import co.edu.uq.estructuras.sga.api.dto.DtosHuesped.SolicitudHuesped;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.EmpleadoDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.PeriodoTemporadaDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.TurnoDto;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva.CobroNocheDto;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva.CotizacionDto;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva.OcupanteDto;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva.ReservaDto;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva.VistaPreviaReembolsoDto;
import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.huespedes.Huesped;
import co.edu.uq.estructuras.sga.core.huespedes.PerfilHuesped;
import co.edu.uq.estructuras.sga.core.huespedes.RegistroEstancia;
import co.edu.uq.estructuras.sga.core.reservas.Reserva;
import co.edu.uq.estructuras.sga.core.tarifas.CobroNoche;
import co.edu.uq.estructuras.sga.core.tarifas.VistaPreviaReembolso;
import co.edu.uq.estructuras.sga.service.ServicioTurnos;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.ConflictoCanalDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.NodoDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.PasilloDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.ResultadoProcesamientoDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.RutaDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.SolicitudExternaDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.TramoRutaDto;
import co.edu.uq.estructuras.sga.estructuras.GrafoPonderado;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.core.folio.Folio;
import co.edu.uq.estructuras.sga.core.folio.MovimientoFolio;
import co.edu.uq.estructuras.sga.core.personal.Empleado;
import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.reservas.Ocupante;
import co.edu.uq.estructuras.sga.core.tarifas.Cotizacion;
import co.edu.uq.estructuras.sga.core.tarifas.PeriodoTemporada;
import co.edu.uq.estructuras.sga.core.tarifas.TarifasTemporada;
import co.edu.uq.estructuras.sga.core.canal.ConflictoCanal;
import co.edu.uq.estructuras.sga.core.canal.SolicitudReservaExterna;
import co.edu.uq.estructuras.sga.core.edificio.NodoEdificio;
import co.edu.uq.estructuras.sga.service.ServicioCanalExterno;
import co.edu.uq.estructuras.sga.service.ServicioMapaEdificio;

/// Convierte objetos del dominio en registros de transporte (y viceversa) para la API REST.
/// Tener esta traducción en un solo lugar evita que el dominio dependa de detalles de JSON.
public final class MapeadorDto {

    /// Clase de utilidades: no se instancia.
    private MapeadorDto() {
    }

    /// Convierte cualquiera de las estructuras propias en un arreglo, que JSON serializa como lista.
    public static <T, R> R[] aArreglo(Iterable<T> origen, Function<T, R> mapeador, IntFunction<R[]> generador) {
        ListaDoblementeEnlazada<R> mapeado = new ListaDoblementeEnlazada<>();
        for (T item : origen) {
            mapeado.agregarUltimo(mapeador.apply(item));
        }
        return mapeado.aArreglo(generador);
    }

    /// Convierte un arreglo de JSON en una lista propia (lista vacía si viene `null`).
    public static <T> ListaDoblementeEnlazada<T> aLista(T[] arreglo) {
        return arreglo == null ? new ListaDoblementeEnlazada<>() : ListaDoblementeEnlazada.de(arreglo);
    }

    // Apartamentos --------------------------------------------------------------

    /// Tarifas del dominio a DTO.
    public static TarifasDto aDto(TarifasTemporada tarifas) {
        return new TarifasDto(tarifas.baja().monto(), tarifas.media().monto(), tarifas.alta().monto());
    }

    /// Tarifas del DTO al dominio.
    public static TarifasTemporada aTarifas(TarifasDto dto) {
        return new TarifasTemporada(Dinero.de(dto.baja()), Dinero.de(dto.media()), Dinero.de(dto.alta()));
    }

    /// Apartamento a DTO; `contadorReservas` dice cuántas reservas tiene.
    public static ApartamentoDto aDto(Apartamento apartamento, ToIntFunction<Apartamento> contadorReservas) {
        return new ApartamentoDto(apartamento.obtenerCodigo(), apartamento.obtenerNombre(), apartamento.obtenerBloque(), apartamento.obtenerPiso(),
                apartamento.obtenerHabitaciones(), apartamento.obtenerCapacidad(), apartamento.obtenerCaracteristicas().aArreglo(String[]::new),
                aDto(apartamento.obtenerTarifas()), apartamento.obtenerEstado(), apartamento.obtenerEstado().nombreMostrado(),
                apartamento.obtenerMotivoFueraDeServicio(), apartamento.obtenerFueraDeServicioDesde(),
                contadorReservas.applyAsInt(apartamento));
    }

    // Huéspedes -----------------------------------------------------------------

    /// Huésped a DTO, con sus favoritos como arreglo.
    public static HuespedDto aDto(Huesped huesped) {
        return new HuespedDto(huesped.obtenerDocumento(), huesped.obtenerNombreCompleto(), huesped.obtenerFechaNacimiento(), huesped.obtenerTelefono(),
                huesped.obtenerCorreo(), huesped.obtenerUrlFoto(), huesped.obtenerCodigosApartamentosFavoritos().aArreglo(String[]::new),
                huesped.obtenerHistorialEstancias().tamano());
    }

    /// Petición de huésped al perfil del dominio.
    public static PerfilHuesped aPerfil(SolicitudHuesped solicitud) {
        return new PerfilHuesped(solicitud.documento(), solicitud.nombreCompleto(), solicitud.fechaNacimiento(), solicitud.telefono(),
                solicitud.correo(), solicitud.urlFoto());
    }

    /// Estancia del historial a DTO.
    public static RegistroEstanciaDto aDto(RegistroEstancia record) {
        return new RegistroEstanciaDto(record.idReserva(), record.codigoApartamento(), record.nombreApartamento(),
                record.estancia().ingreso(), record.estancia().salida(), record.estancia().noches(), record.ocupantes(),
                record.total().monto());
    }

    // Reservas ------------------------------------------------------------------

    /// Ocupante a DTO.
    public static OcupanteDto aDto(Ocupante ocupante) {
        return new OcupanteDto(ocupante.nombreCompleto(), ocupante.fechaNacimiento());
    }

    /// Arreglo de ocupantes del JSON a lista propia del dominio.
    public static ListaDoblementeEnlazada<Ocupante> aOcupantes(OcupanteDto[] dtos) {
        ListaDoblementeEnlazada<Ocupante> ocupantes = new ListaDoblementeEnlazada<>();
        if (dtos != null) {
            for (OcupanteDto dto : dtos) {
                ocupantes.agregarUltimo(new Ocupante(dto.nombreCompleto(), dto.fechaNacimiento()));
            }
        }
        return ocupantes;
    }

    /// Reserva a DTO con su saldo y su vencimiento.
    public static ReservaDto aDto(Reserva reserva) {
        Folio folio = reserva.obtenerFolio();
        return new ReservaDto(reserva.obtenerId(), reserva.obtenerApartamento().obtenerCodigo(),
                reserva.obtenerApartamento().obtenerNombre(), reserva.obtenerTitular().obtenerDocumento(),
                reserva.obtenerTitular().obtenerNombreCompleto(),
                aArreglo(reserva.obtenerOcupantes(), MapeadorDto::aDto, OcupanteDto[]::new),
                reserva.obtenerEstancia().ingreso(), reserva.obtenerEstancia().salida(), reserva.obtenerEstancia().noches(),
                reserva.obtenerCanal(), reserva.obtenerCanal().nombreMostrado(), reserva.obtenerEstado(),
                reserva.obtenerEstado().nombreMostrado(), reserva.obtenerFechaCreacion(), reserva.obtenerFechaVencimiento(),
                reserva.obtenerLlegadaEstimada(), reserva.obtenerCotizacion().total().monto(),
                reserva.anticipoRequerido().monto(), folio.montoPagado().monto(), folio.saldo().monto());
    }

    /// Cotización a DTO con el desglose de noches.
    public static CotizacionDto aDto(String codigoApartamento, Cotizacion cotizacion) {
        return new CotizacionDto(codigoApartamento, cotizacion.estancia().ingreso(), cotizacion.estancia().salida(), cotizacion.estancia().noches(),
                aArreglo(cotizacion.noches(), MapeadorDto::aDto, CobroNocheDto[]::new), cotizacion.total().monto(),
                cotizacion.total().porcentaje(Reserva.PORCENTAJE_ANTICIPO).monto());
    }

    /// Noche liquidada a DTO.
    public static CobroNocheDto aDto(CobroNoche cargo) {
        return new CobroNocheDto(cargo.noche(), cargo.temporada(), cargo.temporada().nombreMostrado(),
                cargo.tarifaPorPersona().monto(), cargo.huespedesQuePagan(), cargo.huespedesGratis(), cargo.subtotal().monto());
    }

    /// Vista previa de reembolso a DTO.
    public static VistaPreviaReembolsoDto aDto(VistaPreviaReembolso vistaPrevia) {
        return new VistaPreviaReembolsoDto(vistaPrevia.idReserva(), vistaPrevia.pagado().monto(), vistaPrevia.porcentaje(),
                vistaPrevia.reembolso().monto(), vistaPrevia.penalidad().monto(), vistaPrevia.diasAntesIngreso());
    }

    // Folio ---------------------------------------------------------------------

    /// Movimiento del folio a DTO.
    public static MovimientoFolioDto aDto(MovimientoFolio movimiento) {
        return new MovimientoFolioDto(movimiento.numero(), movimiento.tipo(), movimiento.tipo().nombreMostrado(), movimiento.monto().monto(),
                movimiento.montoConSigno().monto(), movimiento.descripcion(), movimiento.fechaCreacion(), movimiento.anulaMovimiento());
    }

    /// Folio de la reserva a DTO con totales y saldo.
    public static FolioDto aFolioDto(Reserva reserva) {
        Folio folio = reserva.obtenerFolio();
        MovimientoFolio siguiente = folio.proximoDeshacible();
        return new FolioDto(folio.obtenerNumero(), reserva.obtenerId(), reserva.obtenerEstado().nombreMostrado(),
                aArreglo(folio.obtenerMovimientos(), MapeadorDto::aDto, MovimientoFolioDto[]::new),
                folio.montoCargado().monto(), folio.montoPagado().monto(), folio.saldo().monto(),
                folio.puedeDeshacer(), siguiente == null ? null : siguiente.tipo().nombreMostrado() + " · " + siguiente.descripcion());
    }

    // Operación -----------------------------------------------------------------

    /// Ruta de preparación a DTO.
    public static RutaDto aDto(ServicioMapaEdificio.RutaPreparacion ruta) {
        return new RutaDto(ruta.inicio(), ruta.paradas().aArreglo(String[]::new),
                aArreglo(ruta.tramos(), tramo -> new TramoRutaDto(tramo.desde(), tramo.hasta(), tramo.camino().aArreglo(String[]::new),
                        tramo.metros()), TramoRutaDto[]::new),
                ruta.caminoCompleto().aArreglo(String[]::new), ruta.metrosTotales(), ruta.inalcanzables().aArreglo(String[]::new));
    }

    /// Nodo del mapa a DTO con el estado del apartamento (o `null`).
    public static NodoDto aDto(NodoEdificio nodo, String estadoApartamento) {
        return new NodoDto(nodo.id(), nodo.tipo(), nodo.tipo().nombreMostrado(), nodo.etiqueta(), nodo.bloque(), nodo.piso(),
                nodo.x(), nodo.y(), estadoApartamento);
    }

    /// Pasillo del grafo a DTO.
    public static PasilloDto aDto(GrafoPonderado.Arista<String> arista) {
        return new PasilloDto(arista.desde(), arista.hasta(), arista.peso(), arista.estaAbierto());
    }

    /// Turno a DTO.
    public static TurnoDto aDto(ServicioTurnos.TurnoAsignado turno) {
        return new TurnoDto(turno.recepcionista().obtenerId(), turno.recepcionista().obtenerNombreCompleto(), turno.franja().name(),
                turno.franja().nombreMostrado(), turno.inicio(), turno.fin());
    }

    /// Empleado a DTO.
    public static EmpleadoDto aDto(Empleado empleado) {
        return new EmpleadoDto(empleado.obtenerId(), empleado.obtenerNombreCompleto(), empleado.obtenerRol(),
                empleado.obtenerRol().nombreMostrado(), empleado.obtenerCorreo(), empleado.obtenerTelefono());
    }

    /// Periodo de temporada a DTO.
    public static PeriodoTemporadaDto aDto(PeriodoTemporada periodo) {
        return new PeriodoTemporadaDto(periodo.temporada(), periodo.temporada().nombreMostrado(), periodo.inicio(), periodo.fin());
    }

    /// Solicitud externa en cola a DTO.
    public static SolicitudExternaDto aDto(SolicitudReservaExterna solicitud) {
        return new SolicitudExternaDto(solicitud.idSolicitud(), solicitud.plataforma(), solicitud.codigoApartamento(),
                solicitud.estancia().ingreso(), solicitud.estancia().salida(), solicitud.titular().documento(),
                solicitud.titular().nombreCompleto(), Math.max(solicitud.ocupantes().tamano(), 1), solicitud.fechaRecepcion());
    }

    /// Resultado del procesamiento del canal externo a DTO.
    public static ResultadoProcesamientoDto aDto(ServicioCanalExterno.ResultadoProcesamiento resultado) {
        return new ResultadoProcesamientoDto(resultado.solicitud().idSolicitud(), resultado.solicitud().plataforma(),
                resultado.solicitud().codigoApartamento(), resultado.aceptada(), resultado.idReserva(),
                resultado.conflicto() == null ? null : resultado.conflicto().motivo());
    }

    /// Conflicto del canal externo a DTO.
    public static ConflictoCanalDto aDto(ConflictoCanal conflicto) {
        return new ConflictoCanalDto(conflicto.idSolicitud(), conflicto.plataforma(), conflicto.codigoApartamento(),
                conflicto.estancia().ingreso(), conflicto.estancia().salida(), conflicto.motivo(),
                conflicto.idReservaEnConflicto(), conflicto.fechaDeteccion());
    }
}
