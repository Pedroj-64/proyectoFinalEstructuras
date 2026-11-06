package co.edu.uq.estructuras.sga.core.tarifas;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;

/// Tarifa por persona por noche para cada temporada.
public record TarifasTemporada(Dinero baja, Dinero media, Dinero alta) {

    /// Valida que las tres tarifas existan y no sean negativas (una tarifa en cero sí se permite).
    ///
    /// @throws ReglaNegocioExcepcion si alguna tarifa es `null` o negativa
    public TarifasTemporada {
        if (baja == null || media == null || alta == null
                || baja.monto() < 0 || media.monto() < 0 || alta.monto() < 0) {
            throw new ReglaNegocioExcepcion("Las tarifas deben existir y no pueden ser negativas");
        }
    }

    /// Tarifa de la temporada indicada (switch exhaustivo sobre el enum).
    public Dinero tarifaPara(Temporada temporada) {
        return switch (temporada) {
            case BAJA -> baja;
            case MEDIA -> media;
            case ALTA -> alta;
        };
    }
}
