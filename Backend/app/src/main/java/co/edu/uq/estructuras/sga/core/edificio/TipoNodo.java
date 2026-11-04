package co.edu.uq.estructuras.sga.core.edificio;

public enum TipoNodo {
    APARTAMENTO("Apartamento"),
    RECEPCION("Recepción"),
    ESCALERA("Escaleras"),
    CUARTO_ASEO("Cuarto de aseo");

   
    private final String nombreMostrado;

    TipoNodo(String nombreMostrado) {
        this.nombreMostrado = nombreMostrado;
    }

    public String nombreMostrado() {
        return nombreMostrado;
    }
}
