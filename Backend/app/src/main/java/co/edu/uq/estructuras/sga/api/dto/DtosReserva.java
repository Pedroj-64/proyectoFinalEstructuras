package co.edu.uq.estructuras.sga.api.dto;

import co.edu.uq.estructuras.sga.core.reservas.CanalReserva;
import co.edu.uq.estructuras.sga.core.reservas.EstadoReserva;
import co.edu.uq.estructuras.sga.core.tarifas.Temporada;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/// Cuerpos de petición y respuesta de reservas, cotizaciones y reembolsos.
public final class DtosReserva {

    /// Contenedor de registros: no se instancia.
    private DtosReserva() {
    }

    /// Ocupante con nombre y fecha de nacimiento (define si paga la noche).
    public record OcupanteDto(@NotBlank String nombreCompleto, @NotNull LocalDate fechaNacimiento) {
    }

    /// Reserva tal como la ve la interfaz, con saldo, anticipo y vencimiento.
    public record ReservaDto(String id, String codigoApartamento, String nombreApartamento, String documentoTitular,
                                 String nombreTitular, OcupanteDto[] ocupantes, LocalDate ingreso, LocalDate salida,
                                 long noches, CanalReserva canal, String etiquetaCanal, EstadoReserva estado,
                                 String etiquetaEstado, LocalDateTime fechaCreacion, LocalDateTime fechaVencimiento,
                                 LocalTime llegadaEstimada, long total, long anticipoRequerido, long pagado, long saldo) {
    }

    /// Datos para crear una reserva por portal o directa.
    ///
    /// No confundir con [co.edu.uq.estructuras.sga.core.reservas.SolicitudReserva], que es la solicitud del dominio.
    public record SolicitudReserva(@NotBlank String codigoApartamento, @NotBlank String documentoTitular,
                                     @Valid OcupanteDto[] ocupantes, @NotNull LocalDate ingreso,
                                     @NotNull LocalDate salida, @NotNull CanalReserva canal,
                                     LocalTime llegadaEstimada) {
    }

    /// Datos para buscar acomodo a un grupo grande.
    ///
    /// No confundir con [co.edu.uq.estructuras.sga.core.reservas.SolicitudReservaGrupo], que es la solicitud del dominio.
    public record SolicitudReservaGrupo(@NotBlank String documentoTitular, @NotEmpty @Valid OcupanteDto[] ocupantes,
                                          @NotNull LocalDate ingreso, @NotNull LocalDate salida,
                                          @NotNull CanalReserva canal, LocalTime llegadaEstimada) {
    }

    /// Plan para un grupo: apartamentos escogidos y capacidad total.
    public record PlanGrupoDto(int tamanoGrupo, String[] codigosApartamentos, int capacidadTotal) {
    }

    /// Datos para cotizar una estancia sin reservar.
    public record SolicitudCotizacion(@NotBlank String codigoApartamento, @NotNull LocalDate ingreso, @NotNull LocalDate salida,
                               @NotEmpty @Valid OcupanteDto[] ocupantes) {
    }

    /// Cobro de una noche: temporada, tarifa y personas que pagan.
    public record CobroNocheDto(LocalDate noche, Temporada temporada, String etiquetaTemporada, long tarifaPorPersona,
                                 int huespedesQuePagan, int huespedesGratis, long subtotal) {
    }

    /// Cotización con el desglose noche por noche, el total y el anticipo.
    public record CotizacionDto(String codigoApartamento, LocalDate ingreso, LocalDate salida, long noches,
                           CobroNocheDto[] desglose, long total, long anticipo) {
    }

    /// Vista previa del reembolso si se cancela ahora.
    public record VistaPreviaReembolsoDto(String idReserva, long pagado, int porcentaje, long reembolso, long penalidad,
                                   long diasAntesIngreso) {
    }
}
