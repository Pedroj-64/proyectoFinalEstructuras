package co.edu.uq.estructuras.sga.core.tarifas;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;

/// Precio de una estancia con su desglose noche por noche.
public record Cotizacion(Estancia estancia, ListaDoblementeEnlazada<CobroNoche> noches, Dinero total) {
}
