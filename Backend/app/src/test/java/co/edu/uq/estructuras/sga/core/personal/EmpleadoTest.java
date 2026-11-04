package co.edu.uq.estructuras.sga.core.personal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas del empleado y sus validaciones.
class EmpleadoTest {

    @Test
    @DisplayName("crea el empleado quitando espacios sobrantes en id y nombre")
    void creaEmpleado() {
        Empleado empleado = new Empleado(" 101 ", " Ana Ruiz ", RolEmpleado.RECEPCIONISTA, "ana@uq.edu.co", "3000000");
        assertEquals("101", empleado.obtenerId());
        assertEquals("Ana Ruiz", empleado.obtenerNombreCompleto());
        assertEquals(RolEmpleado.RECEPCIONISTA, empleado.obtenerRol());
    }

    @Test
    @DisplayName("exige identificación, nombre y rol")
    void exigeDatosObligatorios() {
        assertThrows(ReglaNegocioExcepcion.class, () -> new Empleado(" ", "Ana", RolEmpleado.ASEO, null, null));
        assertThrows(ReglaNegocioExcepcion.class, () -> new Empleado("1", "", RolEmpleado.ASEO, null, null));
        assertThrows(ReglaNegocioExcepcion.class, () -> new Empleado("1", "Ana", null, null, null));
    }

    @Test
    @DisplayName("actualizar cambia los datos pero no la identificación")
    void actualizaDatos() {
        Empleado empleado = new Empleado("1", "Ana", RolEmpleado.ASEO, null, null);
        empleado.actualizar("Ana María", RolEmpleado.ADMINISTRADOR, "a@uq.edu.co", "3111111");
        assertEquals("1", empleado.obtenerId());
        assertEquals("Ana María", empleado.obtenerNombreCompleto());
        assertEquals(RolEmpleado.ADMINISTRADOR, empleado.obtenerRol());
    }

    @Test
    @DisplayName("la igualdad y el orden natural dependen solo de la identificación")
    void igualdadYOrden() {
        Empleado a = new Empleado("1", "Ana", RolEmpleado.ASEO, null, null);
        Empleado mismoId = new Empleado("1", "Otra", RolEmpleado.ADMINISTRADOR, null, null);
        Empleado b = new Empleado("2", "Beto", RolEmpleado.ASEO, null, null);
        assertEquals(a, mismoId);
        assertEquals(a.hashCode(), mismoId.hashCode());
        assertNotEquals(a, b);
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    @DisplayName("cada rol expone su nombre en español")
    void nombresDeRol() {
        assertEquals("Recepcionista", RolEmpleado.RECEPCIONISTA.nombreMostrado());
        assertEquals("Administrador", RolEmpleado.ADMINISTRADOR.nombreMostrado());
    }
}
