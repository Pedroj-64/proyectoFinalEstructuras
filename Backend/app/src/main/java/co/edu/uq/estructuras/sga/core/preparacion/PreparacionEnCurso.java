package co.edu.uq.estructuras.sga.core.preparacion;

import java.time.LocalDateTime;

/// Apartamento que un empleado está preparando ahora mismo.
public record PreparacionEnCurso(String codigoApartamento, LocalDateTime fechaInicio) {
}
