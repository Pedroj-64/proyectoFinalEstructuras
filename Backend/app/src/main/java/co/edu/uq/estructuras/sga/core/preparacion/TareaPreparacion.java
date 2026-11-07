package co.edu.uq.estructuras.sga.core.preparacion;

import java.time.LocalDateTime;
import java.time.LocalTime;

/// Apartamento pendiente de preparar. Las llegadas de hoy tienen prioridad 1 y el resto prioridad 2.
///
/// @param codigoApartamento apartamento a preparar
/// @param llegadaHoy si un huésped llega hoy a este apartamento
/// @param proximaLlegada hora estimada de la próxima llegada, o `null` si no hay
/// @param fechaEncolado momento en que entró a la cola
public record TareaPreparacion(String codigoApartamento, boolean llegadaHoy, LocalTime proximaLlegada,
                               LocalDateTime fechaEncolado) {

    /// 1 si hay llegada hoy, 2 en los demás casos.
    public int prioridad() {
        return llegadaHoy ? 1 : 2;
    }
}
