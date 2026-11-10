package co.edu.uq.estructuras.sga.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import co.edu.uq.estructuras.sga.core.tarifas.CalendarioTemporadas;
import co.edu.uq.estructuras.sga.core.tarifas.PoliticaReembolsoEscalonada;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Pruebas de la configuración del dominio sin levantar Spring: se llama cada método `@Bean` a mano.
class ConfiguracionDominioTest {

    private final ConfiguracionDominio configuracion = new ConfiguracionDominio();

    @Test
    @DisplayName("cada método @Bean devuelve un objeto")
    void cadaBeanDevuelveUnObjeto() {
        CalendarioTemporadas calendario = configuracion.calendarioTemporadas();

        assertNotNull(calendario);
        assertNotNull(configuracion.liquidadorEstancia(calendario));
        assertNotNull(configuracion.asignadorGrupos());
        assertNotNull(configuracion.politicaReembolso());
    }

    @Test
    @DisplayName("la política de reembolso que se inyecta es la escalonada")
    void politicaReembolsoEsEscalonada() {
        assertInstanceOf(PoliticaReembolsoEscalonada.class, configuracion.politicaReembolso());
    }
}
