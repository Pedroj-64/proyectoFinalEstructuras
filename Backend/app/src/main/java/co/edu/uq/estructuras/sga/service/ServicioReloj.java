package co.edu.uq.estructuras.sga.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;

import co.edu.uq.estructuras.sga.service.tiempo.ProveedorTiempo;

/// Reloj del sistema con desfase ajustable, para que la demostración pueda "viajar en el tiempo"
/// (por ejemplo, 25 horas adelante para ver vencer una reserva pendiente).
@Service
public class ServicioReloj implements ProveedorTiempo {

    /// Reloj base.
    private final Clock reloj;
    /// Desfase que se suma al reloj base (cero = hora real).
    private Duration desfase = Duration.ZERO;

    /// Reloj real del sistema sin desfase.
    public ServicioReloj() {
        this(Clock.systemDefaultZone());
    }

    /// @param reloj reloj base (las pruebas pasan uno fijo)
    public ServicioReloj(Clock reloj) {
        this.reloj = reloj;
    }

    /// Hora real más el desfase, sin fracciones de segundo.
    @Override
    public synchronized LocalDateTime ahora() {
        return LocalDateTime.now(reloj).plus(desfase).truncatedTo(ChronoUnit.SECONDS);
    }

    /// Mueve el reloj hasta la fecha y hora indicadas.
    public synchronized void viajarA(LocalDateTime destino) {
        desfase = Duration.between(LocalDateTime.now(reloj), destino);
    }

    /// Vuelve a la hora real.
    public synchronized void reinicio() {
        desfase = Duration.ZERO;
    }

    /// Indica si hay un desfase activo (el reloj no es el real).
    public synchronized boolean estaSimulado() {
        return !desfase.isZero();
    }
}
