package co.edu.uq.estructuras.sga.core.huespedes;

import co.edu.uq.estructuras.sga.core.reservas.Ocupante;
import co.edu.uq.estructuras.sga.estructuras.ConjuntoHash;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;

import java.time.LocalDate;

/// Perfil del huésped. Orden natural: por documento de identidad.
public class Huesped implements Comparable<Huesped> {

    private final String documento;
    private String nombreCompleto;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String correo;
    private String urlFoto;
    /// Códigos de apartamentos favoritos (el conjunto evita repetidos).
    private final ConjuntoHash<String> codigosApartamentosFavoritos = new ConjuntoHash<>();
    /// Estancias terminadas, en orden cronológico.
    private final ListaDoblementeEnlazada<RegistroEstancia> historialEstancia = new ListaDoblementeEnlazada<>();

    /// Crea el huésped a partir de su perfil, sin favoritos ni historial.
    public Huesped(PerfilHuesped perfil) {
        if (perfil.documento() == null || perfil.documento().isBlank()) {
            throw new ReglaNegocioExcepcion("El documento del huésped es obligatorio");
        }
        this.documento = perfil.documento().trim();
        actualizar(perfil);
    }

    /// Actualiza el perfil; el documento no cambia.
    public void actualizar(PerfilHuesped perfil) {
        if (perfil.nombreCompleto() == null || perfil.nombreCompleto().isBlank()) {
            throw new ReglaNegocioExcepcion("El nombre del huésped es obligatorio");
        }
        if (perfil.fechaNacimiento() == null) {
            throw new ReglaNegocioExcepcion("La fecha de nacimiento es obligatoria");
        }
        if (perfil.correo() == null || !perfil.correo().contains("@")) {
            throw new ReglaNegocioExcepcion("El correo del huésped no es válido");
        }
        this.nombreCompleto = perfil.nombreCompleto().trim();
        this.fechaNacimiento = perfil.fechaNacimiento();
        this.telefono = perfil.telefono();
        this.correo = perfil.correo().trim();
        this.urlFoto = perfil.urlFoto();
    }

    /// Devuelve `false` cuando el apartamento ya era favorito (el conjunto rechaza repetidos).
    public boolean agregarFavorito(String codigoApartamento) {
        return codigosApartamentosFavoritos.agregar(codigoApartamento);
    }

    /// Quita un favorito. Devuelve si estaba guardado.
    public boolean quitarFavorito(String codigoApartamento) {
        return codigosApartamentosFavoritos.quitar(codigoApartamento);
    }

    /// Agrega una estancia finalizada al historial.
    public void registrarEstancia(RegistroEstancia estancia) {
        historialEstancia.agregarUltimo(estancia);
    }

    /// El huésped como ocupante (para incluirlo en su propia reserva).
    public Ocupante comoOcupante() {
        return new Ocupante(nombreCompleto, fechaNacimiento);
    }

    /// Documento de identidad (llave del árbol de huéspedes).
    public String obtenerDocumento() {
        return documento;
    }

    /// Nombre completo.
    public String obtenerNombreCompleto() {
        return nombreCompleto;
    }

    /// Fecha de nacimiento.
    public LocalDate obtenerFechaNacimiento() {
        return fechaNacimiento;
    }

    /// Teléfono.
    public String obtenerTelefono() {
        return telefono;
    }

    /// Correo electrónico.
    public String obtenerCorreo() {
        return correo;
    }

    /// URL de la foto de perfil (opcional).
    public String obtenerUrlFoto() {
        return urlFoto;
    }

    /// Códigos de apartamentos favoritos (conjunto sin repetidos).
    public ConjuntoHash<String> obtenerCodigosApartamentosFavoritos() {
        return codigosApartamentosFavoritos;
    }

    /// Estancias terminadas, de la más antigua a la más reciente.
    public ListaDoblementeEnlazada<RegistroEstancia> obtenerHistorialEstancias() {
        return historialEstancia;
    }

    /// Orden natural: por documento.
    @Override
    public int compareTo(Huesped otro) {
        return documento.compareTo(otro.documento);
    }

    /// Dos huéspedes son iguales si tienen el mismo documento.
    @Override
    public boolean equals(Object otro) {
        return otro instanceof Huesped huesped && documento.equals(huesped.documento);
    }

    /// Consistente con [#equals(Object)]: usa el documento.
    @Override
    public int hashCode() {
        return documento.hashCode();
    }
}
