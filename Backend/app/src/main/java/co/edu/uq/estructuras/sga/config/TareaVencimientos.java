package co.edu.uq.estructuras.sga.config;

import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.service.ServicioVencimientos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/// Tarea programada que, cada cierto tiempo, cancela las reservas PENDIENTES cuyo plazo para pagar
/// el anticipo ya pasó. El intervalo se lee de la propiedad `sga.vencimiento.intervalo-revision-ms`;
/// si no está definida, se usan 5000 ms (5 segundos).
@Component
public class TareaVencimientos {

    private static final Logger REGISTRO = LoggerFactory.getLogger(TareaVencimientos.class);

    private final ServicioVencimientos vencimientos;

    /// Recibe por inyección el servicio de vencimientos.
    ///
    /// @param vencimientos servicio que vence las reservas pendientes
    public TareaVencimientos(ServicioVencimientos vencimientos) {
        this.vencimientos = vencimientos;
    }

    /// Vence las reservas pendientes cuyo plazo ya pasó y deja en el registro los id cancelados, si hubo alguno.
    /// Se ejecuta por primera vez después de un intervalo y luego espera un intervalo después de terminar cada revisión.
    @Scheduled(fixedDelayString = "${sga.vencimiento.intervalo-revision-ms:5000}",
            initialDelayString = "${sga.vencimiento.intervalo-revision-ms:5000}")
    public void vencerReservasPendientes() {
        ListaDoblementeEnlazada<String> vencida = vencimientos.vencerVencidas();
        if (!vencida.estaVacia()) {
            REGISTRO.info("Reservas vencidas y canceladas: {}", vencida);
        }
    }
}
