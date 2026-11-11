package co.edu.uq.estructuras.sga.service;

import co.edu.uq.estructuras.sga.core.tarifas.TarifasTemporada;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;


/// Datos necesarios para crear o actualizar un apartamento.
public record DatosApartamento(String codigo, String nombre, String bloque, int piso, int habitaciones, int capacidad,
                            ListaDoblementeEnlazada<String> caracteristicas, TarifasTemporada tarifas) {
}
