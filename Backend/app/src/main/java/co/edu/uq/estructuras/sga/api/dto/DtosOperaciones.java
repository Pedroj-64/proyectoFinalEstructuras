package co.edu.uq.estructuras.sga.api.dto;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import co.edu.uq.estructuras.sga.core.edificio.TipoNodo;
import co.edu.uq.estructuras.sga.core.personal.RolEmpleado;
import co.edu.uq.estructuras.sga.core.tarifas.Temporada;

import co.edu.uq.estructuras.sga.api.dto.DtosHuesped.SolicitudHuesped;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva.OcupanteDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/// Cuerpos de los endpoints de preparación, mapa, turnos, temporadas, empleados y canal externo.
public final class DtosOperaciones {

    /// Contenedor de registros: no se instancia.
    private DtosOperaciones() {
    }

    // Preparación ------------------------------------------------------------
    /// Tarea de la cola de preparación con su posición y prioridad.
    public record TareaPreparacionDto(int posicion, String codigoApartamento, String nombreApartamento, String bloque,
                                     int prioridad, boolean llegadaHoy, LocalTime proximaLlegada,
                                     LocalDateTime fechaEncolado) {
    }

    /// Apartamento que se está preparando y desde cuándo.
    public record EnPreparacionDto(String codigoApartamento, String nombreApartamento, LocalDateTime fechaInicio) {
    }

    /// Tramo de la ruta entre dos paradas con el camino de Dijkstra y sus metros.
    public record TramoRutaDto(String desde, String hasta, String[] camino, double metros) {
    }

    /// Ruta de preparación completa: paradas en orden, tramos y distancia total.
    public record RutaDto(String inicio, String[] paradas, TramoRutaDto[] tramos, String[] caminoCompleto, double metrosTotales,
                           String[] inalcanzables) {
    }

    // Mapa del edificio ------------------------------------------------------
    /// Nodo del mapa con coordenadas para dibujarlo y, si es apartamento, su estado.
    public record NodoDto(String id, TipoNodo tipo, String etiquetaTipo, String etiqueta, String bloque, int piso,
                          double x, double y, String estadoApartamento) {
    }

    /// Pasillo entre dos nodos con su longitud y si está abierto.
    public record PasilloDto(String desde, String hasta, double metros, boolean abierto) {
    }

    /// Mapa completo del edificio: nodos y pasillos.
    public record MapaEdificioDto(NodoDto[] nodos, PasilloDto[] pasillos) {
    }

    /// Datos para agregar un nodo al mapa.
    public record SolicitudNodo(@NotBlank String id, @NotNull TipoNodo tipo, @NotBlank String etiqueta, String bloque,
                              int piso, double x, double y) {
    }

    /// Datos para agregar un pasillo.
    public record SolicitudPasillo(@NotBlank String desde, @NotBlank String hasta, @Positive double metros) {
    }

    /// Petición para abrir o cerrar un pasillo.
    public record SolicitudEstadoPasillo(@NotBlank String desde, @NotBlank String hasta, boolean abierto) {
    }

    /// Camino más corto entre dos nodos.
    public record CaminoDto(String[] camino, double metros, boolean alcanzable) {
    }

    /// Apartamentos de un bloque, en el orden en que los visita el recorrido.
    public record GrupoBloqueDto(String bloque, String[] apartamentos) {
    }

    /// Análisis del grafo: componentes conexos, apartamentos inalcanzables y agrupación por bloque.
    public record AnalisisGrafoDto(String[][] componentes, String[] apartamentosInalcanzables, GrupoBloqueDto[] porBloque,
                                   String[] ordenAnchuraPrimero, String[] ordenProfundidadPrimero) {
    }

    // Turnos y empleados -----------------------------------------------------
    /// Turno con su franja, fecha y recepcionista asignado.
    public record TurnoDto(String idRecepcionista, String nombreRecepcionista, String franja, String etiquetaFranja,
                           LocalDateTime inicio, LocalDateTime fin) {
    }

    /// Empleado con su rol traducido.
    public record EmpleadoDto(String id, String nombreCompleto, RolEmpleado rol, String etiquetaRol, String correo,
                              String telefono) {
    }

    /// Datos para registrar o editar un empleado.
    public record SolicitudEmpleado(@NotBlank String id, @NotBlank String nombreCompleto, @NotNull RolEmpleado rol,
                                  String correo, String telefono) {
    }

    // Temporadas -------------------------------------------------------------
    /// Periodo de temporada con sus fechas.
    public record PeriodoTemporadaDto(@NotNull Temporada temporada, String etiquetaTemporada, @NotNull LocalDate inicio,
                                  @NotNull LocalDate fin) {
    }

    /// Temporada que corresponde a una fecha.
    public record TemporadaDeDto(LocalDate fecha, Temporada temporada, String etiquetaTemporada) {
    }

    // Canal externo ----------------------------------------------------------
    /// Solicitud del canal externo que espera en la cola.
    public record SolicitudExternaDto(String idSolicitud, String plataforma, String codigoApartamento, LocalDate ingreso,
                                     LocalDate salida, String documentoTitular, String nombreTitular, int ocupantes,
                                     LocalDateTime fechaRecepcion) {
    }

    /// Datos para que la plataforma simulada envíe una solicitud.
    public record EnvioSolicitudExterna(@NotBlank String plataforma, @NotBlank String codigoApartamento,
                                        @NotNull LocalDate ingreso, @NotNull LocalDate salida,
                                        @NotNull @Valid SolicitudHuesped titular,
                                        @Valid OcupanteDto[] ocupantes) {
    }

    /// Resultado de procesar una solicitud: aceptada con su reserva o rechazada con el motivo.
    public record ResultadoProcesamientoDto(String idSolicitud, String plataforma, String codigoApartamento, boolean aceptada,
                                      String idReserva, String motivo) {
    }

    /// Conflicto del canal externo: solicitud rechazada, motivo y reserva con la que chocó.
    public record ConflictoCanalDto(String idSolicitud, String plataforma, String codigoApartamento, LocalDate ingreso,
                                     LocalDate salida, String motivo, String idReservaEnConflicto,
                                     LocalDateTime fechaDeteccion) {
    }

    // Sistema ----------------------------------------------------------------
    /// Estado del reloj del sistema: hora actual y si está simulado.
    public record RelojDto(LocalDateTime ahora, boolean simulado) {
    }

    /// Petición para mover el reloj simulado a una fecha y hora.
    public record SolicitudReloj(@NotNull LocalDateTime fechaHora) {
    }
}
