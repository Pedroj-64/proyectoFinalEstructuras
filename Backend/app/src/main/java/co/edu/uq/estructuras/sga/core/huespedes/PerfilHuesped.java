package co.edu.uq.estructuras.sga.core.huespedes;

import java.time.LocalDate;

/// Datos personales de un huésped, usados para registrar o actualizar un perfil.
public record PerfilHuesped(String documento, String nombreCompleto, LocalDate fechaNacimiento,
                           String telefono, String correo, String urlFoto) {
}
