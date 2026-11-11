package co.edu.uq.estructuras.sga.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/// Ejecuta cada petición de la API dentro del [CandadoAccesoDatos], así los controladores y servicios
/// pueden usar las estructuras propias (no seguras para hilos) sin llenar todo de `synchronized`.
@Component
public class FiltroAccesoSerializado extends OncePerRequestFilter {

    private final CandadoAccesoDatos candado;

    /// @param lock candado compartido de acceso a datos
    public FiltroAccesoSerializado(CandadoAccesoDatos candado) {
        this.candado = candado;
    }

    /// Solo se serializan las peticiones a `/api/`; los archivos estáticos pasan sin candado.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest solicitud) {
        return !solicitud.getRequestURI().startsWith("/api/");
    }

    /// Ejecuta el resto de la cadena dentro del candado y relanza las excepciones originales.
    @Override
    protected void doFilterInternal(HttpServletRequest solicitud, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        try {
            candado.conCandado(() -> {
                try {
                    cadena.doFilter(solicitud, respuesta);
                    return null;
                } catch (IOException | ServletException excepcion) {
                    throw new FalloFiltro(excepcion);
                }
            });
        } catch (FalloFiltro fallo) {
            if (fallo.getCause() instanceof IOException io) {
                throw io;
            }
            throw (ServletException) fallo.getCause();
        }
    }

    /// Envoltorio para sacar excepciones verificadas de la lambda del candado.
    private static final class FalloFiltro extends RuntimeException {
        /// @param cause excepción original
        private FalloFiltro(Exception causa) {
            super(causa);
        }
    }
}
