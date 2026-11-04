package co.edu.uq.estructuras.sga.core.reservas;

/// Valor en pesos colombianos. Los pesos no tienen centavos, así que un `long` evita errores de redondeo.
public record Dinero(long monto) implements Comparable<Dinero> {

    public static final Dinero CERO = new Dinero(0);

    /// Crea un valor en pesos.
    public static Dinero de(long monto) {
        return new Dinero(monto);
    }

    /// Suma.
    public Dinero mas(Dinero otro) {
        return new Dinero(monto + otro.monto);
    }

    /// Resta.
    public Dinero menos(Dinero otro) {
        return new Dinero(monto - otro.monto);
    }

    /// Multiplica por un entero.
    public Dinero veces(long factor) {
        return new Dinero(monto * factor);
    }

    /// Porcentaje redondeado al peso más cercano (mitad hacia arriba).
    public Dinero porcentaje(int porcentaje) {
        return new Dinero(Math.round(monto * porcentaje / 100.0));
    }

    /// Cambia el signo.
    public Dinero negar() {
        return new Dinero(-monto);
    }

    /// Indica si el valor es mayor que cero.
    public boolean esPositivo() {
        return monto > 0;
    }

    /// Indica si este valor es mayor que `otro`.
    public boolean esMayorQue(Dinero otro) {
        return monto > otro.monto;
    }

    /// El mayor entre este valor y `otro`.
    public Dinero maximo(Dinero otro) {
        return monto >= otro.monto ? this : otro;
    }

    /// Orden natural: por cantidad.
    @Override
    public int compareTo(Dinero otro) {
        return Long.compare(monto, otro.monto);
    }

    /// Devuelve el monto precedido de `$` y sin separador de miles, por ejemplo `$120000` (o `$-5000` si es negativo).
    @Override
    public String toString() {
        return "$" + monto;
    }
}
