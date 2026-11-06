package co.edu.uq.estructuras.sga.core.tarifas;

/// Temporadas tarifarias. BAJA es la temporada base para cualquier noche fuera de un periodo.
public enum Temporada {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta");

    /// Nombre en español para la interfaz.
    private final String nombreMostrado;

    Temporada(String nombreMostrado) {
        this.nombreMostrado = nombreMostrado;
    }

    /// Nombre en español para mostrar.
    public String nombreMostrado() {
        return nombreMostrado;
    }
}
