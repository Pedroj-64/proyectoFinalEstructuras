package co.edu.uq.estructuras.sga.service;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.reservas.Dinero;

/// Resultado de la búsqueda de disponibilidad: un apartamento libre con la tarifa de la primera noche
/// y un total estimado para la estancia consultada.
///
/// @param apartamento apartamento libre
/// @param tarifaPorPersona tarifa por persona de la temporada de la primera noche
/// @param totalEstimado precio estimado de toda la estancia, suponiendo que todos los huéspedes pagan
/// @param cantidadReservas cuántas reservas tiene el apartamento, en cualquier estado
public record ApartamentoDisponible(Apartamento apartamento, Dinero tarifaPorPersona, Dinero totalEstimado,
                                    int cantidadReservas) {
}
