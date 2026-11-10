package co.edu.uq.estructuras.sga.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/// Permite que el servidor de desarrollo del frontend (Vite) llame a la API.
@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {

    private final String[] origenesPermitidos;

    /// @param origenesPermitidos orígenes CORS (propiedad `sga.cors.origenes-permitidos`; por defecto Vite)
    public ConfiguracionWeb(@Value("${sga.cors.origenes-permitidos:http://localhost:5173}") String[] origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void addCorsMappings(CorsRegistry registro) {
        registro.addMapping("/api/**")
                .allowedOrigins(origenesPermitidos)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
    }
}
