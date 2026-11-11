package co.edu.uq.estructuras.sga.service;

import co.edu.uq.estructuras.sga.core.tarifas.CalendarioTemporadas;
import co.edu.uq.estructuras.sga.core.tarifas.PeriodoTemporada;
import co.edu.uq.estructuras.sga.core.tarifas.Temporada;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;


import java.time.LocalDate;
import org.springframework.stereotype.Service;

/// Fechas de temporada (administrador). Las tarifas de cada apartamento se manejan en [ServicioApartamentos].
@Service
public class ServicioTemporadas {

    private final CalendarioTemporadas calendario;

    /// Recibe por inyección el calendario de temporadas.
    public ServicioTemporadas(CalendarioTemporadas calendario) {
        this.calendario = calendario;
    }

    /// Periodos de temporada actuales.
    public ListaDoblementeEnlazada<PeriodoTemporada> periodos() {
        return calendario.periodos();
    }

    /// Reemplaza todos los periodos (todo o nada).
    public void reemplazarPeriodos(ListaDoblementeEnlazada<PeriodoTemporada> periodos) {
        calendario.reemplazarPeriodos(periodos);
    }

    /// Agrega un periodo que no se solape con los existentes.
    public void agregarPeriodo(PeriodoTemporada periodo) {
        calendario.agregarPeriodo(periodo);
    }

    /// Temporada a la que pertenece una fecha.
    public Temporada temporadaDe(LocalDate fecha) {
        return calendario.temporadaDe(fecha);
    }

    /// Quita todos los periodos (todo pasa a temporada baja).
    public void vaciar() {
        calendario.vaciar();
    }
}
