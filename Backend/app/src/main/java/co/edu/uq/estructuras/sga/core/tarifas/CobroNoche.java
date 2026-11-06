package co.edu.uq.estructuras.sga.core.tarifas;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;

import java.time.LocalDate;

/// Precio de una sola noche de una estancia.
public record CobroNoche(LocalDate noche, Temporada temporada, Dinero tarifaPorPersona,
                          int huespedesQuePagan, int huespedesGratis, Dinero subtotal) {
}
