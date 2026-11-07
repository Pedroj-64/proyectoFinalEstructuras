package co.edu.uq.estructuras.sga.core.preparacion;

import java.util.Comparator;

/// Orden de la cola de preparación: primero las llegadas de hoy, luego la hora de llegada más
/// cercana (sin hora va al final) y, a igualdad, la que entró antes (FIFO).
public final class ComparadorTareaPreparacion implements Comparator<TareaPreparacion> {

    /// Instancia compartida: el comparador no tiene estado.
    public static final ComparadorTareaPreparacion INSTANCIA = new ComparadorTareaPreparacion();

    private ComparadorTareaPreparacion() {
    }

    @Override
    public int compare(TareaPreparacion a, TareaPreparacion b) {
        int porPrioridad = Integer.compare(a.prioridad(), b.prioridad());
        if (porPrioridad != 0) {
            return porPrioridad;
        }
        if (a.proximaLlegada() != null && b.proximaLlegada() != null) {
            int porHora = a.proximaLlegada().compareTo(b.proximaLlegada());
            if (porHora != 0) {
                return porHora;
            }
        } else if (a.proximaLlegada() != null) {
            return -1;
        } else if (b.proximaLlegada() != null) {
            return 1;
        }
        return a.fechaEncolado().compareTo(b.fechaEncolado());
    }
}
