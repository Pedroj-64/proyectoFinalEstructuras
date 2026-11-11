package co.edu.uq.estructuras.sga.config;


import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/// Las estructuras propias no son seguras para hilos. El sistema atiende un solo alojamiento desde
/// un escritorio, así que serializar el acceso con un único candado justo es simple y seguro: cada
/// petición HTTP y cada tarea programada se ejecuta dentro de él.
@Component
public class CandadoAccesoDatos {

    /// Candado justo: atiende las peticiones en el orden en que llegan.
    private final ReentrantLock candado = new ReentrantLock(true);

    /// Ejecuta la acción con el candado tomado y devuelve su resultado.
    public <T> T conCandado(Supplier<T> accion) {
        candado.lock();
        try {
            return accion.get();
        } finally {
            candado.unlock();
        }
    }

    /// Ejecuta la acción con el candado tomado.
    public void ejecutarConCandado(Runnable accion) {
        conCandado(() -> {
            accion.run();
            return null;
        });
    }
}
