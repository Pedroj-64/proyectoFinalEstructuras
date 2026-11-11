package co.edu.uq.estructuras.sga.escenario;

import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.core.tarifas.Temporada;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Pruebas del intérprete del archivo de escenario (fechas relativas y errores con número de línea).
class InterpreteEscenarioTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 3, 10, 9, 0);
    private final InterpreteEscenario interprete = new InterpreteEscenario();

    @Test
    @DisplayName("interpreta fechas relativas a hoy y los campos por nombre")
    void fechasRelativas() {
        Escenario escenario = interprete.interpretar("""
                {
                  "temporadas": [{"temporada": "ALTA", "desde": "hoy+1", "hasta": "hoy+5"}],
                  "reservas": [{
                    "apartamento": "A-101", "titular": "123", "entrada": "hoy-2", "salida": "hoy",
                    "canal": "DIRECTO", "estado": "FINALIZADA", "horaLlegada": "15:00", "pagado": 1000,
                    "ocupantes": [{"nombre": "Ana", "fechaNacimiento": "1990-01-01"}],
                    "creada": "hoy-9 08:30"
                  }]
                }
                """, AHORA);
        assertEquals(Temporada.ALTA, escenario.temporadas().obtenerPrimero().temporada());
        assertEquals(AHORA.toLocalDate().plusDays(1), escenario.temporadas().obtenerPrimero().inicio());
        assertEquals(AHORA.toLocalDate().minusDays(2), escenario.reservas().obtenerPrimero().estancia().ingreso());
        assertEquals(AHORA.toLocalDate().minusDays(9).atTime(8, 30), escenario.reservas().obtenerPrimero().fechaCreacion());
        assertEquals(1, escenario.reservas().obtenerPrimero().ocupantes().tamano());
    }

    @Test
    @DisplayName("informa dónde está el error y qué campo es")
    void reportaUbicacionYCampo() {
        ReglaNegocioExcepcion error = assertThrows(ReglaNegocioExcepcion.class, () -> interprete.interpretar("""
                {"apartamentos": [
                  {"codigo": "A-100", "nombre": "Uno", "bloque": "A", "piso": 1, "dormitorios": 1, "capacidad": 2,
                   "tarifas": {"baja": 1, "media": 1, "alta": 1}},
                  {"codigo": "A-101", "nombre": "Dos", "bloque": "A", "piso": "uno", "dormitorios": 1, "capacidad": 2,
                   "tarifas": {"baja": 1, "media": 1, "alta": 1}}
                ]}
                """, AHORA));
        assertTrue(error.getMessage().contains("apartamentos[1]"), error.getMessage());
        assertTrue(error.getMessage().contains("'piso'"), error.getMessage());
    }

    @Test
    @DisplayName("rechaza un valor de enumeración inválido, una sección desconocida y JSON mal formado")
    void rechazaDatosInvalidos() {
        ReglaNegocioExcepcion enumeracion = assertThrows(ReglaNegocioExcepcion.class, () -> interprete.interpretar("""
                {"empleados": [{"id": "E-1", "nombre": "Ana", "rol": "JEFE"}]}
                """, AHORA));
        assertTrue(enumeracion.getMessage().contains("empleados[0]") && enumeracion.getMessage().contains("RECEPCIONISTA"),
                enumeracion.getMessage());
        assertThrows(ReglaNegocioExcepcion.class, () -> interprete.interpretar("{\"cosas\": []}", AHORA));
        assertThrows(ReglaNegocioExcepcion.class, () -> interprete.interpretar("{ no es json", AHORA));
    }

    @Test
    @DisplayName("el escenario por defecto del proyecto se interpreta completo")
    void escenarioPorDefecto() throws Exception {
        String contenido = new String(getClass().getResourceAsStream("/escenario/escenario-por-defecto.json").readAllBytes(),
                java.nio.charset.StandardCharsets.UTF_8);
        Escenario escenario = interprete.interpretar(contenido, AHORA);
        assertEquals(12, escenario.apartamentos().tamano());
        assertEquals(19, escenario.nodos().tamano());
        assertEquals(19, escenario.reservas().tamano());
        assertEquals(3, escenario.solicitudesExternas().tamano());
    }
}
