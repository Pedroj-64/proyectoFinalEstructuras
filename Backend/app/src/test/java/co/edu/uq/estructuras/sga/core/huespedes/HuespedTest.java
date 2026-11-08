package co.edu.uq.estructuras.sga.core.huespedes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.core.reservas.Ocupante;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas del huésped, su perfil, favoritos e historial de estancias.
class HuespedTest {

    /// Perfil válido de apoyo para las pruebas.
    private static PerfilHuesped perfil(String documento) {
        return new PerfilHuesped(documento, "Ana Ruiz", LocalDate.of(1990, 5, 20),
                "3001234567", "ana@uq.edu.co", null);
    }

    @Test
    @DisplayName("crea el huésped quitando espacios sobrantes en documento y nombre")
    void creaHuesped() {
        Huesped huesped = new Huesped(new PerfilHuesped(" 123 ", " Ana Ruiz ", LocalDate.of(1990, 5, 20),
                "3001234567", " ana@uq.edu.co ", "foto.png"));

        assertEquals("123", huesped.obtenerDocumento());
        assertEquals("Ana Ruiz", huesped.obtenerNombreCompleto());
        assertEquals(LocalDate.of(1990, 5, 20), huesped.obtenerFechaNacimiento());
        assertEquals("3001234567", huesped.obtenerTelefono());
        assertEquals("ana@uq.edu.co", huesped.obtenerCorreo());
        assertEquals("foto.png", huesped.obtenerUrlFoto());
    }

    @Test
    @DisplayName("exige documento, nombre, fecha de nacimiento y correo válido")
    void exigeDatosObligatorios() {
        assertThrows(ReglaNegocioExcepcion.class, () -> new Huesped(new PerfilHuesped(
                " ", "Ana", LocalDate.of(1990, 1, 1), null, "ana@uq.edu.co", null)));
        assertThrows(ReglaNegocioExcepcion.class, () -> new Huesped(new PerfilHuesped(
                "1", " ", LocalDate.of(1990, 1, 1), null, "ana@uq.edu.co", null)));
        assertThrows(ReglaNegocioExcepcion.class, () -> new Huesped(new PerfilHuesped(
                "1", "Ana", null, null, "ana@uq.edu.co", null)));
        assertThrows(ReglaNegocioExcepcion.class, () -> new Huesped(new PerfilHuesped(
                "1", "Ana", LocalDate.of(1990, 1, 1), null, null, null)));
        assertThrows(ReglaNegocioExcepcion.class, () -> new Huesped(new PerfilHuesped(
                "1", "Ana", LocalDate.of(1990, 1, 1), null, "correo-sin-arroba", null)));
    }

    @Test
    @DisplayName("actualizar cambia el perfil pero no el documento")
    void actualizaPerfil() {
        Huesped huesped = new Huesped(perfil("1"));
        huesped.actualizar(new PerfilHuesped("999", "Ana María", LocalDate.of(1991, 6, 15),
                "3111111", "maria@uq.edu.co", "nueva.png"));

        assertEquals("1", huesped.obtenerDocumento());
        assertEquals("Ana María", huesped.obtenerNombreCompleto());
        assertEquals(LocalDate.of(1991, 6, 15), huesped.obtenerFechaNacimiento());
        assertEquals("3111111", huesped.obtenerTelefono());
        assertEquals("maria@uq.edu.co", huesped.obtenerCorreo());
        assertEquals("nueva.png", huesped.obtenerUrlFoto());
    }

    @Test
    @DisplayName("los favoritos no admiten repetidos y se pueden quitar")
    void favoritosSinRepetidos() {
        Huesped huesped = new Huesped(perfil("1"));

        assertTrue(huesped.agregarFavorito("A-101"));
        assertFalse(huesped.agregarFavorito("A-101"));
        assertTrue(huesped.obtenerCodigosApartamentosFavoritos().contiene("A-101"));
        assertEquals(1, huesped.obtenerCodigosApartamentosFavoritos().tamano());

        assertTrue(huesped.quitarFavorito("A-101"));
        assertFalse(huesped.quitarFavorito("A-101"));
        assertTrue(huesped.obtenerCodigosApartamentosFavoritos().estaVacia());
    }

    @Test
    @DisplayName("el historial guarda las estancias de la más antigua a la más reciente")
    void historialEnOrden() {
        Huesped huesped = new Huesped(perfil("1"));
        RegistroEstancia primera = new RegistroEstancia("R-1", "A-101", "Suite A-101",
                new Estancia(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 12)), 2, Dinero.de(200_000));
        RegistroEstancia segunda = new RegistroEstancia("R-2", "A-102", "Suite A-102",
                new Estancia(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 3)), 1, Dinero.de(150_000));

        huesped.registrarEstancia(primera);
        huesped.registrarEstancia(segunda);

        assertEquals(2, huesped.obtenerHistorialEstancias().tamano());
        assertEquals("R-1", huesped.obtenerHistorialEstancias().obtenerPrimero().idReserva());
        assertEquals("R-2", huesped.obtenerHistorialEstancias().obtenerUltimo().idReserva());
    }

    @Test
    @DisplayName("el huésped se puede usar como ocupante de su propia reserva")
    void comoOcupante() {
        Huesped huesped = new Huesped(perfil("1"));
        Ocupante ocupante = huesped.comoOcupante();

        assertEquals("Ana Ruiz", ocupante.nombreCompleto());
        assertEquals(LocalDate.of(1990, 5, 20), ocupante.fechaNacimiento());
    }

    @Test
    @DisplayName("la igualdad y el orden natural dependen solo del documento")
    void igualdadYOrden() {
        Huesped a = new Huesped(perfil("1"));
        Huesped mismoDocumento = new Huesped(new PerfilHuesped("1", "Otra Persona",
                LocalDate.of(1985, 1, 1), null, "otra@uq.edu.co", null));
        Huesped b = new Huesped(perfil("2"));

        assertEquals(a, mismoDocumento);
        assertEquals(a.hashCode(), mismoDocumento.hashCode());
        assertNotEquals(a, b);
        assertTrue(a.compareTo(b) < 0);
    }
}
