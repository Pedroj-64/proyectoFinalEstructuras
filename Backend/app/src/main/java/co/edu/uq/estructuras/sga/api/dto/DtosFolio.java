package co.edu.uq.estructuras.sga.api.dto;

import co.edu.uq.estructuras.sga.core.folio.TipoMovimientoFolio;

import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

/// Cuerpos de petición y respuesta del folio.
public final class DtosFolio {

    /// Contenedor de registros: no se instancia.
    private DtosFolio() {
    }

    /// Movimiento del folio con su monto sin signo (`monto`) y con signo (`montoConSigno`, positivo si aumenta el saldo).
    public record MovimientoFolioDto(int numero, TipoMovimientoFolio tipo, String etiquetaTipo, long monto, long montoConSigno,
                                String descripcion, LocalDateTime fechaCreacion, Integer anulaMovimiento) {
    }

    /// Folio completo: movimientos, totales, saldo y si se puede deshacer.
    public record FolioDto(String numero, String idReserva, String estadoReserva, MovimientoFolioDto[] movimientos,
                           long cargado, long pagado, long saldo, boolean puedeDeshacer, String proximoDeshacer) {
    }

    /// Petición de cargo o pago manual en recepción.
    public record SolicitudMovimiento(@Positive long monto, String descripcion) {
    }
}
