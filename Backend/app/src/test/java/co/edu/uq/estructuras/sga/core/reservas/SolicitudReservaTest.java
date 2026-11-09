package co.edu.uq.estructuras.sga.core.reservas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de que las solicitudes de reserva, individuales y de grupo, conservan sus datos.
class SolicitudReservaTest {

    private static final Estancia ESTANCIA = new Estancia(LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 23));
    private static final LocalTime LLEGADA = LocalTime.of(15, 30);

    private static final Ocupante ANA = new Ocupante("Ana Gómez", LocalDate.of(1990, 5, 20));
    private static final Ocupante LUIS = new Ocupante("Luis Gómez", LocalDate.of(1988, 8, 3));
    private static final Ocupante SARA = new Ocupante("Sara Gómez", LocalDate.of(2022, 1, 15));

    @Test
    @DisplayName("una solicitud individual guarda todos sus datos")
    void solicitudGuardaSusDatos() {
        ListaDoblementeEnlazada<Ocupante> ocupantes = ListaDoblementeEnlazada.de(ANA, LUIS);
        SolicitudReserva solicitud =
                new SolicitudReserva("A-101", "1094123456", ocupantes, ESTANCIA, CanalReserva.DIRECTO, LLEGADA);

        assertEquals("A-101", solicitud.codigoApartamento());
        assertEquals("1094123456", solicitud.documentoTitular());
        assertSame(ocupantes, solicitud.ocupantes());
        assertEquals(ESTANCIA, solicitud.estancia());
        assertEquals(CanalReserva.DIRECTO, solicitud.canal());
        assertEquals(LLEGADA, solicitud.llegadaEstimada());
    }

    @Test
    @DisplayName("una solicitud de grupo guarda sus datos y sus 3 ocupantes en orden")
    void solicitudDeGrupoConTresOcupantes() {
        SolicitudReservaGrupo grupo = new SolicitudReservaGrupo("1094123456",
                ListaDoblementeEnlazada.de(ANA, LUIS, SARA), ESTANCIA, CanalReserva.PORTAL, LLEGADA);

        assertEquals("1094123456", grupo.documentoTitular());
        assertEquals(3, grupo.ocupantes().tamano());
        assertEquals(ANA, grupo.ocupantes().obtener(0));
        assertEquals(LUIS, grupo.ocupantes().obtener(1));
        assertEquals(SARA, grupo.ocupantes().obtener(2));
        assertEquals(ESTANCIA, grupo.estancia());
        assertEquals(CanalReserva.PORTAL, grupo.canal());
        assertEquals(LLEGADA, grupo.llegadaEstimada());
    }

    @Test
    @DisplayName("dos solicitudes con los mismos datos son iguales")
    void solicitudesConLosMismosDatosSonIguales() {
        // ListaDoblementeEnlazada no redefine equals (compara por referencia),
        // así que las dos solicitudes comparten la misma lista de ocupantes.
        ListaDoblementeEnlazada<Ocupante> ocupantes = ListaDoblementeEnlazada.de(ANA, LUIS);
        SolicitudReserva una =
                new SolicitudReserva("A-101", "1094123456", ocupantes, ESTANCIA, CanalReserva.DIRECTO, LLEGADA);
        SolicitudReserva otra = new SolicitudReserva("A-101", "1094123456", ocupantes,
                new Estancia(LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 23)), CanalReserva.DIRECTO,
                LocalTime.of(15, 30));

        assertEquals(una, otra);
        assertEquals(una.hashCode(), otra.hashCode());

        ListaDoblementeEnlazada<Ocupante> grupo = ListaDoblementeEnlazada.de(ANA, LUIS, SARA);
        assertEquals(new SolicitudReservaGrupo("1094123456", grupo, ESTANCIA, CanalReserva.PORTAL, LLEGADA),
                new SolicitudReservaGrupo("1094123456", grupo, ESTANCIA, CanalReserva.PORTAL, LLEGADA));
    }
}
