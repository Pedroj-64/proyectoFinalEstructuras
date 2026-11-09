package co.edu.uq.estructuras.sga.api.dto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import co.edu.uq.estructuras.sga.api.dto.DtosFolio.SolicitudMovimiento;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva.OcupanteDto;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva.SolicitudCotizacion;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de que los DTO de reservas y del folio conservan los datos que llegan en las peticiones.
class DtosReservaTest {

    private static final OcupanteDto ANA = new OcupanteDto("Ana Gómez", LocalDate.of(1990, 5, 20));
    private static final OcupanteDto SARA = new OcupanteDto("Sara Gómez", LocalDate.of(2022, 1, 15));

    @Test
    @DisplayName("un ocupante guarda su nombre y su fecha de nacimiento")
    void ocupanteGuardaSusDatos() {
        assertEquals("Ana Gómez", ANA.nombreCompleto());
        assertEquals(LocalDate.of(1990, 5, 20), ANA.fechaNacimiento());
    }

    @Test
    @DisplayName("una solicitud de cotización guarda el apartamento, las fechas y los ocupantes en orden")
    void solicitudCotizacionGuardaSusDatos() {
        SolicitudCotizacion solicitud = new SolicitudCotizacion("A-101", LocalDate.of(2026, 12, 20),
                LocalDate.of(2026, 12, 23), new OcupanteDto[] {ANA, SARA});

        assertEquals("A-101", solicitud.codigoApartamento());
        assertEquals(LocalDate.of(2026, 12, 20), solicitud.ingreso());
        assertEquals(LocalDate.of(2026, 12, 23), solicitud.salida());
        assertArrayEquals(new OcupanteDto[] {ANA, SARA}, solicitud.ocupantes());
    }

    @Test
    @DisplayName("una solicitud de movimiento guarda el monto y la descripción")
    void solicitudMovimientoGuardaSusDatos() {
        SolicitudMovimiento solicitud = new SolicitudMovimiento(50_000, "Minibar");

        assertEquals(50_000L, solicitud.monto());
        assertEquals("Minibar", solicitud.descripcion());
    }
}
