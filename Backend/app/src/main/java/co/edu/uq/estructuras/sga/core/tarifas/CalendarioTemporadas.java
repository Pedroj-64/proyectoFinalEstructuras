package co.edu.uq.estructuras.sga.core.tarifas;

import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.RangoFechasInvalidoExcepcion;

import java.time.LocalDate;

/// Periodos de temporada definidos por el administrador. Cualquier fecha fuera de un periodo es temporada baja.
public class CalendarioTemporadas {

    /// Periodos de temporada sin solapamientos.
    private final ListaDoblementeEnlazada<PeriodoTemporada> periodos = new ListaDoblementeEnlazada<>();

    /// Temporada de una fecha; si no cae en ningún periodo, es temporada baja.
    public Temporada temporadaDe(LocalDate fecha) {
        PeriodoTemporada periodo = periodos.buscar(candidato -> candidato.contiene(fecha));
        return periodo == null ? Temporada.BAJA : periodo.temporada();
    }

    /// Agrega un periodo. Los periodos no se pueden solapar; si no, una noche tendría dos precios.
    ///
    /// @throws RangoFechasInvalidoExcepcion si `periodo` es `null` o comparte al menos un día con un periodo existente
    public void agregarPeriodo(PeriodoTemporada periodo) {
        if (periodo == null) {
            throw new RangoFechasInvalidoExcepcion("El periodo es obligatorio");
        }
        PeriodoTemporada choque = periodos.buscar(existente -> existente.solapa(periodo));
        if (choque != null) {
            throw new RangoFechasInvalidoExcepcion("La temporada %s (%s a %s) se cruza con %s (%s a %s)".formatted(
                    periodo.temporada().nombreMostrado(), periodo.inicio(), periodo.fin(),
                    choque.temporada().nombreMostrado(), choque.inicio(), choque.fin()));
        }
        periodos.agregarUltimo(periodo);
    }

    /// Reemplaza todos los periodos de forma atómica: si uno es inválido, se conserva el calendario anterior.
    ///
    /// @throws RangoFechasInvalidoExcepcion si alguno de los `nuevosPeriodos` es `null` o si dos de ellos comparten al menos un día
    public void reemplazarPeriodos(Iterable<PeriodoTemporada> nuevosPeriodos) {
        CalendarioTemporadas candidato = new CalendarioTemporadas();
        for (PeriodoTemporada periodo : nuevosPeriodos) {
            candidato.agregarPeriodo(periodo);
        }
        periodos.vaciar();
        periodos.agregarTodos(candidato.periodos);
    }

    /// Copia de los periodos definidos, ordenada por fecha de inicio; modificarla no altera el calendario.
    public ListaDoblementeEnlazada<PeriodoTemporada> periodos() {
        return periodos.ordenada((a, b) -> a.inicio().compareTo(b.inicio()));
    }

    /// Quita todos los periodos.
    public void vaciar() {
        periodos.vaciar();
    }
}
