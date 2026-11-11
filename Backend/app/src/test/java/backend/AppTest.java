package backend;

import co.edu.uq.estructuras.sga.App;
import co.edu.uq.estructuras.sga.service.ServicioReservas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/// Verifica que la aplicación de Spring Boot arranca: todos los beans se pueden crear y conectar,
/// incluido el escenario de demostración que se carga al iniciar.
@SpringBootTest(classes = App.class)
class AppTest {

    @Autowired
    private ServicioReservas reservas;

    @Test
    void elContextoArrancaConTodosLosServicios() {
        assertNotNull(reservas);
    }
}
