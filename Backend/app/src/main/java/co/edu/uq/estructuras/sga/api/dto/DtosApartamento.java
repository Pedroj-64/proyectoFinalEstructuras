package co.edu.uq.estructuras.sga.api.dto;

import co.edu.uq.estructuras.sga.core.apartamentos.EstadoApartamento;

import java.time.LocalDate;

/// Cuerpos de respuesta de apartamentos y tarifas.
public final class DtosApartamento {

    /// Contenedor de registros: no se instancia.
    private DtosApartamento() {
    }

    /// Tarifa por persona y noche en cada temporada.
    public record TarifasDto(long baja, long media, long alta) {
    }

    /// Apartamento con su estado, tarifas y cantidad de reservas.
    public record ApartamentoDto(String codigo, String nombre, String bloque, int piso, int habitaciones, int capacidad,
                                 String[] caracteristicas, TarifasDto tarifas, EstadoApartamento estado, String etiquetaEstado,
                                 String motivoFueraDeServicio, LocalDate fueraDeServicioDesde, int cantidadReservas) {
    }
}
