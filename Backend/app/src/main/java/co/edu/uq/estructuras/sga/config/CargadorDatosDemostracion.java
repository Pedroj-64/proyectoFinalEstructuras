package co.edu.uq.estructuras.sga.config;

import co.edu.uq.estructuras.sga.service.ServicioEscenario;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/// Carga el escenario de demostración incluido al arrancar (se desactiva con `sga.demo-datos.activo=false`).
@Component
public class CargadorDatosDemostracion implements ApplicationRunner {

    public static final String ESCENARIO_POR_DEFECTO = "escenario/escenario-por-defecto.json";

    private final ServicioEscenario escenarios;
    private final CandadoAccesoDatos candado;
    /// Si es `false` (propiedad `sga.demo-datos.activo`), no se carga el escenario al arrancar.
    private final boolean activo;

    /// Recibe por inyección el servicio de escenarios, el candado y si la carga al arrancar está activa.
    public CargadorDatosDemostracion(ServicioEscenario escenarios, CandadoAccesoDatos candado,
                          @Value("${sga.datos-demo.activo}") boolean activo) {
        this.escenarios = escenarios;
        this.candado = candado;
        this.activo = activo;
    }

    /// Carga el escenario al terminar el arranque de Spring.
    @Override
    public void run(ApplicationArguments args) {
        if (!activo) {
            return;
        }
        var resumen = candado.conCandado(() -> escenarios.cargar(leerEscenarioPorDefecto()));
        // Java 25: java.lang.IO da salida simple por consola sin System.out.
        IO.println("SGA listo - escenario de prueba cargado: " + resumen);
    }

    /// Lee el escenario de demostración incluido en los recursos.
    public static String leerEscenarioPorDefecto() {
        try (var input = new ClassPathResource(ESCENARIO_POR_DEFECTO).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException excepcion) {
            throw new UncheckedIOException("No se pudo leer el escenario por defecto", excepcion);
        }
    }
}
