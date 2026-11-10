package co.edu.uq.estructuras.sga.core.reservas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.tarifas.TarifasTemporada;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.CapacidadExcedidaExcepcion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas del backtracking que reparte un grupo en la menor cantidad de apartamentos reales.
class AsignadorGruposTest {

    private final AsignadorGrupos asignador = new AsignadorGrupos();

    /// Apartamento real con la capacidad dada y las mismas tarifas para todos.
    private static Apartamento apartamento(String codigo, int capacidad) {
        return new Apartamento(codigo, "Suite " + codigo, "A", 1, Math.max(1, capacidad / 2), capacidad,
                ListaDoblementeEnlazada.de("Wifi"),
                new TarifasTemporada(Dinero.de(100_000), Dinero.de(120_000), Dinero.de(150_000)));
    }

    /// Suma la capacidad de los apartamentos elegidos.
    private static int camas(ListaDoblementeEnlazada<Apartamento> elegidos) {
        int camas = 0;
        for (Apartamento apartamento : elegidos) {
            camas += apartamento.obtenerCapacidad();
        }
        return camas;
    }

    @Test
    @DisplayName("si el grupo cabe en un apartamento, usa solo uno")
    void unSoloApartamento() {
        ListaDoblementeEnlazada<Apartamento> disponibles =
                ListaDoblementeEnlazada.de(apartamento("A-201", 2), apartamento("A-401", 4), apartamento("A-601", 6));

        ListaDoblementeEnlazada<Apartamento> elegidos = asignador.asignar(disponibles, 5);

        assertEquals(1, elegidos.tamano());
        assertEquals("A-601", elegidos.obtener(0).obtenerCodigo(), "solo el de 6 camas aloja a 5 personas");
    }

    @Test
    @DisplayName("usa el mínimo de apartamentos y, entre esos, el que desperdicia menos camas")
    void minimoApartamentosConMenorDesperdicio() {
        // Con 11 personas no basta uno. Con dos: 7+6 sobran 2, 7+5 sobra 1 y 6+5 no sobra ninguna.
        // Tomar primero el más grande (7) no daría la mejor opción.
        ListaDoblementeEnlazada<Apartamento> disponibles = ListaDoblementeEnlazada.de(apartamento("B-3", 3),
                apartamento("B-7", 7), apartamento("B-5", 5), apartamento("B-6", 6));

        ListaDoblementeEnlazada<Apartamento> elegidos = asignador.asignar(disponibles, 11);

        assertEquals(2, elegidos.tamano());
        assertEquals(11, camas(elegidos));
        assertEquals("B-5", elegidos.obtener(0).obtenerCodigo());
        assertEquals("B-6", elegidos.obtener(1).obtenerCodigo());
    }

    @Test
    @DisplayName("un grupo mayor que toda la capacidad, o vacío, lanza CapacidadExcedidaExcepcion")
    void capacidadInsuficiente() {
        ListaDoblementeEnlazada<Apartamento> disponibles =
                ListaDoblementeEnlazada.de(apartamento("C-1", 2), apartamento("C-2", 2));

        assertThrows(CapacidadExcedidaExcepcion.class, () -> asignador.asignar(disponibles, 5));
        assertThrows(CapacidadExcedidaExcepcion.class, () -> asignador.asignar(disponibles, 0));
    }

    @Test
    @DisplayName("la lista de disponibles original no se modifica")
    void noModificaLosDisponibles() {
        ListaDoblementeEnlazada<Apartamento> disponibles =
                ListaDoblementeEnlazada.de(apartamento("D-2", 2), apartamento("D-4", 4), apartamento("D-6", 6));

        asignador.asignar(disponibles, 9);

        assertEquals(3, disponibles.tamano());
        assertEquals("D-2", disponibles.obtener(0).obtenerCodigo());
        assertEquals("D-4", disponibles.obtener(1).obtenerCodigo());
        assertEquals("D-6", disponibles.obtener(2).obtenerCodigo());
    }
}
