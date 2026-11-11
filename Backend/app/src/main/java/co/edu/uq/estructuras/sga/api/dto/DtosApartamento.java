package co.edu.uq.estructuras.sga.api.dto;

import co.edu.uq.estructuras.sga.core.apartamentos.EstadoApartamento;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

/// Cuerpos de respuesta de apartamentos y tarifas.
public final class DtosApartamento {

    /// Contenedor de registros: no se instancia.
    private DtosApartamento() {
    }

    /// Tarifa por persona y noche en cada temporada.
    public record TarifasDto(@PositiveOrZero long baja, @PositiveOrZero long media, @PositiveOrZero long alta) {
    }

    /// Apartamento con su estado, tarifas y cantidad de reservas.
    public record ApartamentoDto(String codigo, String nombre, String bloque, int piso, int habitaciones, int capacidad,
                                 String[] caracteristicas, TarifasDto tarifas, EstadoApartamento estado, String etiquetaEstado,
                                 String motivoFueraDeServicio, LocalDate fueraDeServicioDesde, int cantidadReservas) {
    }

    /// Datos para crear o actualizar un apartamento.
    public record SolicitudApartamento(@NotBlank String codigo, @NotBlank String nombre, String bloque, int piso,
                                       @Min(1) int habitaciones, @Min(1) int capacidad, String[] caracteristicas,
                                       @NotNull @Valid TarifasDto tarifas) {
    }

    /// Cambio de estado operativo; el motivo es obligatorio solo al pasar a fuera de servicio.
    public record SolicitudCambioEstado(@NotNull EstadoApartamento estado, String motivo) {
    }

    /// Apartamento libre en un rango de fechas, con la tarifa de la primera noche y el total estimado.
    public record ApartamentoDisponibleDto(ApartamentoDto apartamento, long tarifaPorPersona, long totalEstimado) {
    }
}
