package co.edu.uq.estructuras.sga.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uq.estructuras.sga.core.personal.Empleado;
import co.edu.uq.estructuras.sga.core.personal.RolEmpleado;
import co.edu.uq.estructuras.sga.excepciones.NoEncontradoExcepcion;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.repository.RepositorioEmpleadosMemoria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de los casos de uso del personal.
class ServicioEmpleadosTest {

    private ServicioEmpleados servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioEmpleados(new RepositorioEmpleadosMemoria());
    }

    @Test
    @DisplayName("crea un empleado y lo deja disponible en el repositorio")
    void creaEmpleado() {
        Empleado empleado = servicio.crear("101", "Ana Ruiz", RolEmpleado.RECEPCIONISTA, "ana@uq.edu.co", "300");

        assertEquals("101", empleado.obtenerId());
        assertEquals(1, servicio.buscarTodos().tamano());
        assertEquals(empleado, servicio.buscar("101"));
    }

    @Test
    @DisplayName("no permite repetir la identificación de un empleado")
    void noRepiteIdentificacion() {
        servicio.crear("101", "Ana", RolEmpleado.RECEPCIONISTA, null, null);

        assertThrows(ReglaNegocioExcepcion.class,
                () -> servicio.crear("101", "Otra", RolEmpleado.ASEO, null, null));
    }

    @Test
    @DisplayName("actualiza los datos de un empleado existente")
    void actualizaEmpleado() {
        servicio.crear("101", "Ana", RolEmpleado.RECEPCIONISTA, null, null);

        Empleado empleado = servicio.actualizar("101", "Ana María", RolEmpleado.ADMINISTRADOR, "ana@uq.edu.co", "311");

        assertEquals("Ana María", empleado.obtenerNombreCompleto());
        assertEquals(RolEmpleado.ADMINISTRADOR, empleado.obtenerRol());
    }

    @Test
    @DisplayName("elimina un empleado y falla si no existe")
    void eliminaEmpleado() {
        servicio.crear("101", "Ana", RolEmpleado.RECEPCIONISTA, null, null);

        servicio.eliminar("101");

        assertEquals(0, servicio.buscarTodos().tamano());
        assertThrows(NoEncontradoExcepcion.class, () -> servicio.eliminar("101"));
    }

    @Test
    @DisplayName("buscar lanza NoEncontradoExcepcion cuando la identificación no existe")
    void buscarInexistente() {
        assertThrows(NoEncontradoExcepcion.class, () -> servicio.buscar("999"));
    }

    @Test
    @DisplayName("recepcionistas filtra solo el rol que participa en los turnos")
    void filtraRecepcionistas() {
        servicio.crear("101", "Ana", RolEmpleado.RECEPCIONISTA, null, null);
        servicio.crear("102", "Beto", RolEmpleado.ASEO, null, null);
        servicio.crear("103", "Carla", RolEmpleado.RECEPCIONISTA, null, null);
        servicio.crear("104", "Diana", RolEmpleado.ADMINISTRADOR, null, null);

        var recepcionistas = servicio.recepcionistas();

        assertEquals(2, recepcionistas.tamano());
        assertEquals("101", recepcionistas.obtener(0).obtenerId());
        assertEquals("103", recepcionistas.obtener(1).obtenerId());
    }
}
