package co.edu.uq.estructuras.sga.config;

import co.edu.uq.estructuras.sga.core.reservas.AsignadorGrupos;
import co.edu.uq.estructuras.sga.core.tarifas.CalendarioTemporadas;
import co.edu.uq.estructuras.sga.core.tarifas.LiquidadorEstancia;
import co.edu.uq.estructuras.sga.core.tarifas.PoliticaReembolso;
import co.edu.uq.estructuras.sga.core.tarifas.PoliticaReembolsoEscalonada;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/// Registra los objetos puros del dominio como beans de Spring. Las clases del dominio no tienen
/// anotaciones de Spring, así se pueden probar y reutilizar sin el framework; esta clase es el único
/// lugar donde se decide qué implementación se inyecta en los servicios.
@Configuration
public class ConfiguracionDominio {

    /// Calendario de temporadas, compartido por el liquidador y el servicio de disponibilidad.
    ///
    /// @return un calendario vacío (toda fecha es temporada baja hasta que se agreguen periodos)
    @Bean
    public CalendarioTemporadas calendarioTemporadas() {
        return new CalendarioTemporadas();
    }

    /// Liquidador de estancias; usa el mismo calendario que el resto de la aplicación.
    ///
    /// @param calendarioTemporadas calendario que dice la temporada de cada noche
    /// @return el liquidador
    @Bean
    public LiquidadorEstancia liquidadorEstancia(CalendarioTemporadas calendarioTemporadas) {
        return new LiquidadorEstancia(calendarioTemporadas);
    }

    /// Asignador que reparte grupos grandes en apartamentos con backtracking.
    ///
    /// @return el asignador
    @Bean
    public AsignadorGrupos asignadorGrupos() {
        return new AsignadorGrupos();
    }

    /// Política de reembolso que se inyecta donde se pida una [PoliticaReembolso] (patrón Strategy):
    /// la escalonada, que devuelve 100 %, 50 % o 0 % según los días que faltan para la entrada.
    ///
    /// @return la política de reembolso escalonada
    @Bean
    public PoliticaReembolso politicaReembolso() {
        return new PoliticaReembolsoEscalonada();
    }
}
