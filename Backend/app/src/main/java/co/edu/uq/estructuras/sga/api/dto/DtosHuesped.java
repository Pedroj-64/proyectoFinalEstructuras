package co.edu.uq.estructuras.sga.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/// Cuerpos de petición y respuesta de huéspedes.
public final class DtosHuesped {

    /// Contenedor de registros: no se instancia.
    private DtosHuesped() {
    }

    /// Huésped con sus apartamentos favoritos y cuántas estancias tiene.
    public record HuespedDto(String documento, String nombreCompleto, LocalDate fechaNacimiento, String telefono, String correo,
                             String urlFoto, String[] favoritos, int cantidadEstancias) {
    }

    /// Datos para registrar o editar un huésped.
    public record SolicitudHuesped(@NotBlank String documento, @NotBlank String nombreCompleto, @NotNull LocalDate fechaNacimiento,
                                   String telefono, String correo, String urlFoto) {
    }

    /// Estancia del historial del huésped.
    public record RegistroEstanciaDto(String idReserva, String codigoApartamento, String nombreApartamento, LocalDate ingreso,
                                      LocalDate salida, long noches, int ocupantes, long total) {
    }
}
