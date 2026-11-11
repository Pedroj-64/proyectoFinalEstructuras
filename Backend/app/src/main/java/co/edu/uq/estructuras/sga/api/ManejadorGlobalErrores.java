package co.edu.uq.estructuras.sga.api;

import co.edu.uq.estructuras.sga.excepciones.CapacidadExcedidaExcepcion;
import co.edu.uq.estructuras.sga.excepciones.DominioExcepcion;
import co.edu.uq.estructuras.sga.excepciones.EstructuraVaciaExcepcion;
import co.edu.uq.estructuras.sga.excepciones.NoEncontradoExcepcion;
import co.edu.uq.estructuras.sga.excepciones.RangoFechasInvalidoExcepcion;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.excepciones.ReservaSolapadaExcepcion;
import co.edu.uq.estructuras.sga.excepciones.TransicionEstadoInvalidaExcepcion;


import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/// Traduce las excepciones a respuestas "problem details" (RFC 9457) con mensajes en español.
@RestControllerAdvice
public class ManejadorGlobalErrores {

    /// Errores del dominio: 404, 409 o 422 según el tipo, con código y mensaje en español.
    @ExceptionHandler(DominioExcepcion.class)
    public ProblemDetail manejarDominio(DominioExcepcion excepcion) {
        // DominioExcepcion es sellada: este switch es exhaustivo y no necesita rama default.
        HttpStatus estado = switch (excepcion) {
            case NoEncontradoExcepcion _ -> HttpStatus.NOT_FOUND;
            case ReservaSolapadaExcepcion _, TransicionEstadoInvalidaExcepcion _ -> HttpStatus.CONFLICT;
            case CapacidadExcedidaExcepcion _, RangoFechasInvalidoExcepcion _, ReglaNegocioExcepcion _ ->
                    HttpStatus.UNPROCESSABLE_CONTENT;
        };
        String titulo = switch (excepcion) {
            case NoEncontradoExcepcion _ -> "No encontrado";
            case ReservaSolapadaExcepcion _ -> "Reserva solapada";
            case TransicionEstadoInvalidaExcepcion _ -> "Transición de estado inválida";
            case CapacidadExcedidaExcepcion _ -> "Capacidad excedida";
            case RangoFechasInvalidoExcepcion _ -> "Fechas inválidas";
            case ReglaNegocioExcepcion _ -> "Regla de negocio";
        };
        return problema(estado, titulo, excepcion.getMessage(), excepcion.getClass().getSimpleName());
    }

    /// Consulta sobre una estructura vacía.
    @ExceptionHandler(EstructuraVaciaExcepcion.class)
    public ProblemDetail manejarVacia(EstructuraVaciaExcepcion excepcion) {
        return problema(HttpStatus.CONFLICT, "Estructura vacía", excepcion.getMessage(), "EstructuraVaciaExcepcion");
    }

    /// Errores de validación de los datos recibidos, con la lista de campos inválidos.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarValidacion(MethodArgumentNotValidException excepcion) {
        StringBuilder detalle = new StringBuilder();
        excepcion.getBindingResult().getFieldErrors().forEach(error ->
                detalle.append(detalle.isEmpty() ? "" : "; ").append(error.getField()).append(": ")
                        .append(error.getDefaultMessage()));
        return problema(HttpStatus.BAD_REQUEST, "Datos inválidos", detalle.toString(), "ValidationError");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    /// Peticiones mal formadas (JSON inválido, parámetros con formato incorrecto).
    public ProblemDetail manejarSolicitudInvalida(Exception excepcion) {
        return problema(HttpStatus.BAD_REQUEST, "Solicitud mal formada",
                "Revise el formato de los datos enviados (fechas AAAA-MM-DD, números, valores permitidos)",
                excepcion.getClass().getSimpleName());
    }

    /// Arma la respuesta *problem details* con título, detalle en español y un código para el frontend.
    private static ProblemDetail problema(HttpStatus estado, String titulo, String detalle, String codigo) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        problema.setProperty("codigo", codigo);
        return problema;
    }
}
