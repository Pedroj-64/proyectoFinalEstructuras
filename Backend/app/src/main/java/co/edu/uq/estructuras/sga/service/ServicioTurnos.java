package co.edu.uq.estructuras.sga.service;

import co.edu.uq.estructuras.sga.core.personal.Empleado;
import co.edu.uq.estructuras.sga.estructuras.ListaCircular;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.service.tiempo.ProveedorTiempo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Service;

/// Turnos de recepción. Hay tres turnos de 8 horas al día y los recepcionistas rotan sobre
/// ellos en una lista circular: después del último vuelve el primero.
@Service
public class ServicioTurnos {

    /// Las tres franjas de 8 horas del día con su hora de inicio.
    public enum FranjaTurno {
        MANANA("Mañana", LocalTime.of(6, 0)),
        TARDE("Tarde", LocalTime.of(14, 0)),
        NOCHE("Noche", LocalTime.of(22, 0));

        /// Nombre en español.
        private final String nombreMostrado;
        /// Hora de inicio.
        private final LocalTime inicio;

        FranjaTurno(String nombreMostrado, LocalTime inicio) {
            this.nombreMostrado = nombreMostrado;
            this.inicio = inicio;
        }

        /// Nombre en español de la franja.
        public String nombreMostrado() {
            return nombreMostrado;
        }

        /// Hora de inicio de la franja.
        public LocalTime inicio() {
            return inicio;
        }
    }

    /// Turno asignado: recepcionista, franja y horario.
    public record TurnoAsignado(Empleado recepcionista, FranjaTurno franja, LocalDateTime inicio, LocalDateTime fin) {
    }

    /// Cantidad de turnos por día (tres).
    private static final int TURNOS_POR_DIA = FranjaTurno.values().length;

    private final ProveedorTiempo tiempo;

    /// Recibe por inyección el reloj del sistema.
    public ServicioTurnos(ProveedorTiempo tiempo) {
        this.tiempo = tiempo;
    }

    /// Turno que está en curso según el reloj del sistema.
    public TurnoAsignado actual(ListaDoblementeEnlazada<Empleado> recepcionistas) {
        return proximos(recepcionistas, 1).obtenerPrimero();
    }

    /// El turno actual seguido de los siguientes `cantidad - 1`.
    public ListaDoblementeEnlazada<TurnoAsignado> proximos(ListaDoblementeEnlazada<Empleado> recepcionistas, int cantidad) {
        ListaCircular<Empleado> rotacion = rotacion(recepcionistas);
        LocalDateTime ahora = tiempo.ahora();
        // Un turno de noche que empezó ayer a las 22:00 sigue en curso antes de las 06:00.
        LocalDate diaTurno = ahora.toLocalTime().isBefore(FranjaTurno.MANANA.inicio())
                ? ahora.toLocalDate().minusDays(1)
                : ahora.toLocalDate();
        int indiceFranja = indiceFranjaEn(ahora.toLocalTime());
        long turnoAbsoluto = diaTurno.toEpochDay() * TURNOS_POR_DIA + indiceFranja;

        ListaDoblementeEnlazada<TurnoAsignado> resultado = new ListaDoblementeEnlazada<>();
        for (int i = 0; i < Math.max(cantidad, 1); i++) {
            long numeroTurno = turnoAbsoluto + i;
            LocalDate dia = LocalDate.ofEpochDay(Math.floorDiv(numeroTurno, TURNOS_POR_DIA));
            FranjaTurno franja = FranjaTurno.values()[(int) Math.floorMod(numeroTurno, TURNOS_POR_DIA)];
            LocalDateTime inicio = dia.atTime(franja.inicio());
            resultado.agregarUltimo(new TurnoAsignado(rotacion.obtenerRotado(numeroTurno), franja, inicio, inicio.plusHours(8)));
        }
        return resultado;
    }

    /// Arma la lista circular con los recepcionistas en orden; falla si no hay ninguno.
    private ListaCircular<Empleado> rotacion(ListaDoblementeEnlazada<Empleado> recepcionistas) {
        ListaCircular<Empleado> rotacion = new ListaCircular<>();
        for (Empleado recepcionista : recepcionistas) {
            rotacion.agregar(recepcionista);
        }
        if (rotacion.estaVacia()) {
            throw new ReglaNegocioExcepcion("No hay recepcionistas registrados para asignar turnos");
        }
        return rotacion;
    }

    /// Índice de la franja que contiene la hora dada (antes de las 06:00 sigue siendo el turno de noche).
    private static int indiceFranjaEn(LocalTime tiempo) {
        if (tiempo.isBefore(FranjaTurno.MANANA.inicio())) {
            return FranjaTurno.NOCHE.ordinal();
        }
        if (tiempo.isBefore(FranjaTurno.TARDE.inicio())) {
            return FranjaTurno.MANANA.ordinal();
        }
        return tiempo.isBefore(FranjaTurno.NOCHE.inicio()) ? FranjaTurno.TARDE.ordinal() : FranjaTurno.NOCHE.ordinal();
    }
}
