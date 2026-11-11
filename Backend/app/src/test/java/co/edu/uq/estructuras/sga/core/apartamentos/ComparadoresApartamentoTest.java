package co.edu.uq.estructuras.sga.core.apartamentos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.tarifas.TarifasTemporada;
import co.edu.uq.estructuras.sga.core.tarifas.Temporada;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de los órdenes alternativos de apartamentos (precio, capacidad, habitaciones y reservas).
class ComparadoresApartamentoTest {

    private static Apartamento apartamento(String codigo, int habitaciones, int capacidad, long tarifaBaja) {
        return new Apartamento(codigo, "Suite " + codigo, "A", 1, habitaciones, capacidad, ListaDoblementeEnlazada.de("Wifi"),
                new TarifasTemporada(Dinero.de(tarifaBaja), Dinero.de(tarifaBaja + 10_000), Dinero.de(tarifaBaja + 20_000)));
    }

    private static final Apartamento A = apartamento("A-1", 1, 2, 120_000);
    private static final Apartamento B = apartamento("A-2", 3, 6, 90_000);
    private static final Apartamento C = apartamento("A-3", 2, 4, 90_000);

    private static String codigos(ListaDoblementeEnlazada<Apartamento> lista) {
        StringBuilder texto = new StringBuilder();
        for (Apartamento apartamento : lista) {
            texto.append(apartamento.obtenerCodigo()).append(' ');
        }
        return texto.toString().trim();
    }

    private static ListaDoblementeEnlazada<Apartamento> todos() {
        return ListaDoblementeEnlazada.de(A, B, C);
    }

    @Test
    @DisplayName("por precio: el más barato primero y los empates por código")
    void porPrecio() {
        assertEquals("A-2 A-3 A-1", codigos(todos().ordenada(ComparadoresApartamento.porPrecio(Temporada.BAJA))));
    }

    @Test
    @DisplayName("por capacidad y por habitaciones: de mayor a menor")
    void porCapacidadYHabitaciones() {
        assertEquals("A-2 A-3 A-1", codigos(todos().ordenada(ComparadoresApartamento.porCapacidad())));
        assertEquals("A-2 A-3 A-1", codigos(todos().ordenada(ComparadoresApartamento.porHabitaciones())));
    }

    @Test
    @DisplayName("por cantidad de reservas: los más reservados primero")
    void porReservas() {
        ListaDoblementeEnlazada<Apartamento> orden = todos().ordenada(
                ComparadoresApartamento.porCantidadReservas(apartamento -> apartamento == C ? 5 : 1));
        assertEquals("A-3 A-1 A-2", codigos(orden));
    }
}
