package co.edu.uq.estructuras.sga.core.apartamentos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.tarifas.TarifasTemporada;
import co.edu.uq.estructuras.sga.core.tarifas.Temporada;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.excepciones.TransicionEstadoInvalidaExcepcion;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas del apartamento: datos, tarifas y ciclo de vida operativo.
class ApartamentoTest {

    /// Tarifas de apoyo: baja 100.000, media 120.000 y alta 150.000 por persona.
    private static TarifasTemporada tarifas() {
        return new TarifasTemporada(Dinero.de(100_000), Dinero.de(120_000), Dinero.de(150_000));
    }

    /// Apartamento válido de apoyo.
    private static Apartamento apartamento() {
        return new Apartamento(" a-101 ", "Suite A-101", " A ", 1, 2, 4,
                ListaDoblementeEnlazada.de("Wifi", "Balcón"), tarifas());
    }

    @Test
    @DisplayName("crea el apartamento en estado Preparado y normaliza código y nombre")
    void creaApartamento() {
        Apartamento apartamento = apartamento();

        assertEquals("A-101", apartamento.obtenerCodigo());
        assertEquals("Suite A-101", apartamento.obtenerNombre());
        assertEquals("A", apartamento.obtenerBloque());
        assertEquals(EstadoApartamento.PREPARADO, apartamento.obtenerEstado());
        assertTrue(apartamento.esReservable());
        assertEquals(2, apartamento.obtenerCaracteristicas().tamano());
    }

    @Test
    @DisplayName("exige código, nombre, capacidad, habitaciones y tarifas")
    void exigeDatosObligatorios() {
        assertThrows(ReglaNegocioExcepcion.class, () -> new Apartamento(
                " ", "Suite", "A", 1, 1, 1, null, tarifas()));
        assertThrows(ReglaNegocioExcepcion.class, () -> new Apartamento(
                "A-1", " ", "A", 1, 1, 1, null, tarifas()));
        assertThrows(ReglaNegocioExcepcion.class, () -> new Apartamento(
                "A-1", "Suite", "A", 1, 0, 1, null, tarifas()));
        assertThrows(ReglaNegocioExcepcion.class, () -> new Apartamento(
                "A-1", "Suite", "A", 1, 1, 0, null, tarifas()));
        assertThrows(ReglaNegocioExcepcion.class, () -> new Apartamento(
                "A-1", "Suite", "A", 1, 1, 1, null, null));
    }

    @Test
    @DisplayName("actualiza detalles y tarifas sin cambiar el código")
    void actualizaDetallesYTarifas() {
        Apartamento apartamento = apartamento();
        TarifasTemporada nuevas = new TarifasTemporada(Dinero.de(110_000), Dinero.de(130_000), Dinero.de(160_000));

        apartamento.actualizarDetalles("Suite Renovada", "B", 2, 3, 6, ListaDoblementeEnlazada.de("Jacuzzi"));
        apartamento.actualizarTarifas(nuevas);

        assertEquals("A-101", apartamento.obtenerCodigo());
        assertEquals("Suite Renovada", apartamento.obtenerNombre());
        assertEquals("B", apartamento.obtenerBloque());
        assertEquals(2, apartamento.obtenerPiso());
        assertEquals(3, apartamento.obtenerHabitaciones());
        assertEquals(6, apartamento.obtenerCapacidad());
        assertEquals(1, apartamento.obtenerCaracteristicas().tamano());
        assertEquals(Dinero.de(130_000), apartamento.tarifaPara(Temporada.MEDIA));
    }

    @Test
    @DisplayName("las características devueltas son una copia y no alteran el apartamento")
    void caracteristicasSonCopia() {
        Apartamento apartamento = apartamento();
        apartamento.obtenerCaracteristicas().agregarUltimo("Piscina");

        assertEquals(2, apartamento.obtenerCaracteristicas().tamano());
    }

    @Test
    @DisplayName("recorre el ciclo Preparado → Ocupado → Pendiente → En preparación → Preparado")
    void cicloDeVida() {
        Apartamento apartamento = apartamento();

        apartamento.cambiarEstado(EstadoApartamento.OCUPADO);
        assertEquals(EstadoApartamento.OCUPADO, apartamento.obtenerEstado());
        // Sigue siendo reservable para fechas futuras; solo se bloquea fuera de servicio.
        assertTrue(apartamento.esReservable());

        apartamento.cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION);
        apartamento.cambiarEstado(EstadoApartamento.EN_PREPARACION);
        apartamento.cambiarEstado(EstadoApartamento.PREPARADO);

        assertEquals(EstadoApartamento.PREPARADO, apartamento.obtenerEstado());
        assertTrue(apartamento.esReservable());
    }

    @Test
    @DisplayName("rechaza transiciones que el ciclo de vida no permite")
    void rechazaTransicionesInvalidas() {
        Apartamento apartamento = apartamento();
        apartamento.cambiarEstado(EstadoApartamento.OCUPADO);

        assertThrows(TransicionEstadoInvalidaExcepcion.class,
                () -> apartamento.cambiarEstado(EstadoApartamento.PREPARADO));
        assertThrows(ReglaNegocioExcepcion.class,
                () -> apartamento.cambiarEstado(EstadoApartamento.FUERA_DE_SERVICIO));
        assertThrows(TransicionEstadoInvalidaExcepcion.class,
                () -> apartamento.marcarFueraDeServicio("Fuga de agua", LocalDate.of(2026, 11, 10)));
    }

    @Test
    @DisplayName("marcar fuera de servicio exige motivo y guarda la fecha")
    void fueraDeServicio() {
        Apartamento apartamento = apartamento();
        LocalDate desde = LocalDate.of(2026, 11, 10);

        apartamento.marcarFueraDeServicio("Fuga de agua", desde);

        assertEquals(EstadoApartamento.FUERA_DE_SERVICIO, apartamento.obtenerEstado());
        assertEquals("Fuga de agua", apartamento.obtenerMotivoFueraDeServicio());
        assertEquals(desde, apartamento.obtenerFueraDeServicioDesde());
        assertFalse(apartamento.esReservable());
        assertThrows(ReglaNegocioExcepcion.class, () -> apartamento.marcarFueraDeServicio(" ", desde));
    }

    @Test
    @DisplayName("restaurar estado se usa al cargar un escenario y conserva el motivo si está fuera de servicio")
    void restauraEstado() {
        Apartamento apartamento = apartamento();
        LocalDate desde = LocalDate.of(2026, 11, 10);

        apartamento.restaurarEstado(EstadoApartamento.FUERA_DE_SERVICIO, "Mantenimiento", desde);
        assertEquals("Mantenimiento", apartamento.obtenerMotivoFueraDeServicio());
        assertEquals(desde, apartamento.obtenerFueraDeServicioDesde());

        apartamento.restaurarEstado(EstadoApartamento.PREPARADO, "Mantenimiento", desde);
        assertNull(apartamento.obtenerMotivoFueraDeServicio());
        assertNull(apartamento.obtenerFueraDeServicioDesde());
    }

    @Test
    @DisplayName("la igualdad y el orden natural dependen solo del código")
    void igualdadYOrden() {
        Apartamento a = new Apartamento("A-1", "Suite 1", "A", 1, 1, 2, null, tarifas());
        Apartamento mismoCodigo = new Apartamento("a-1", "Otra", "B", 2, 2, 4, null, tarifas());
        Apartamento b = new Apartamento("A-2", "Suite 2", "A", 1, 1, 2, null, tarifas());

        assertEquals(a, mismoCodigo);
        assertEquals(a.hashCode(), mismoCodigo.hashCode());
        assertNotEquals(a, b);
        assertTrue(a.compareTo(b) < 0);
    }
}
