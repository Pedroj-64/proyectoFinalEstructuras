package co.edu.uq.estructuras.sga.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uq.estructuras.sga.excepciones.NoEncontradoExcepcion;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.core.preparacion.TareaPreparacion;

class ServicioPreparacionTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 8, 0);
    private ServicioPreparacion servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioPreparacion();
        servicio.encolar(new TareaPreparacion("B1", false, null, T0));
        servicio.encolar(new TareaPreparacion("A1", true, LocalTime.of(15, 0), T0.plusMinutes(1)));
        servicio.encolar(new TareaPreparacion("A2", true, LocalTime.of(12, 0), T0.plusMinutes(2)));
    }

    @Test
    @DisplayName("inicia primero la llegada de hoy más temprana")
    void iniciaLaMasUrgente() {
        assertEquals("A2", servicio.iniciarSiguiente(T0).codigoApartamento());
        assertEquals("A1", servicio.iniciarSiguiente(T0).codigoApartamento());
        assertEquals(1, servicio.preparacionesEnCola());
    }

    @Test
    @DisplayName("no permite encolar dos veces el mismo apartamento")
    void sinDuplicados() {
        assertThrows(ReglaNegocioExcepcion.class, () -> servicio.encolar(new TareaPreparacion("B1", false, null, T0)));
        servicio.iniciarSiguiente(T0);
        assertThrows(ReglaNegocioExcepcion.class, () -> servicio.encolar(new TareaPreparacion("A2", true, null, T0)));
    }

    @Test
    @DisplayName("terminar acumula los minutos promedio")
    void terminar() {
        servicio.iniciarSiguiente(T0);
        servicio.terminar("A2", T0.plusMinutes(30));
        servicio.iniciarSiguiente(T0);
        servicio.terminar("A1", T0.plusMinutes(50));
        assertEquals(2, servicio.preparacionesTerminadas());
        assertEquals(40.0, servicio.minutosPromedio());
        assertThrows(NoEncontradoExcepcion.class, () -> servicio.terminar("A2", T0));
    }

    @Test
    @DisplayName("iniciar con la cola vacía lanza ReglaNegocioExcepcion")
    void colaVacia() {
        servicio.cancelar("B1");
        servicio.cancelar("A1");
        servicio.cancelar("A2");
        assertThrows(ReglaNegocioExcepcion.class, () -> servicio.iniciarSiguiente(T0));
    }

    @Test
    @DisplayName("agrupa por prioridad: hoy primero, luego el resto")
    void grupos() {
        var grupos = servicio.gruposPorPrioridad();
        assertArrayEquals(new String[] {"A2", "A1"}, grupos.obtener(0).aArreglo(String[]::new));
        assertArrayEquals(new String[] {"B1"}, grupos.obtener(1).aArreglo(String[]::new));
    }
}
