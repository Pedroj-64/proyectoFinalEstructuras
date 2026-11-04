package co.edu.uq.estructuras.sga.core.folio;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.estructuras.PilaEnlazada;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;

import java.time.LocalDateTime;

/// Cuenta de una reserva. Los movimientos solo se agregan; "deshacer" registra el movimiento contrario.
///
/// Cada cargo o pago hecho por recepción se apila en `pilaDeshacer`. `deshacerUltimo()` saca el último
/// y registra su movimiento contrario, así el historial nunca se altera.
public class Folio {

    /// Número del folio.
    private final String numero;
    /// Movimientos en orden de registro; solo se agregan, nunca se borran.
    private final ListaDoblementeEnlazada<MovimientoFolio> movimientos = new ListaDoblementeEnlazada<>();
    /// Operaciones de recepción que se pueden deshacer; la cima es la última.
    private final PilaEnlazada<MovimientoFolio> pilaDeshacer = new PilaEnlazada<>();

    /// Crea un folio vacío con su número.
    public Folio(String numero) {
        this.numero = numero;
    }

    /// Cargo registrado por recepción (se puede deshacer).
    ///
    /// @throws ReglaNegocioExcepcion si `monto` es `null`, cero o negativo
    public MovimientoFolio cargo(Dinero monto, String descripcion, LocalDateTime en) {
        MovimientoFolio movimiento = append(TipoMovimientoFolio.CARGO, monto, descripcion, en, null);
        pilaDeshacer.apilar(movimiento);
        return movimiento;
    }

    /// Pago registrado por recepción o por el huésped (se puede deshacer).
    ///
    /// @throws ReglaNegocioExcepcion si `monto` es `null`, cero o negativo
    public MovimientoFolio pagar(Dinero monto, String descripcion, LocalDateTime en) {
        MovimientoFolio movimiento = append(TipoMovimientoFolio.PAGO, monto, descripcion, en, null);
        pilaDeshacer.apilar(movimiento);
        return movimiento;
    }

    /// Movimiento generado por el propio sistema (cargo de alojamiento, reembolsos). No se puede deshacer.
    ///
    /// @throws ReglaNegocioExcepcion si `monto` es `null`, cero o negativo
    public MovimientoFolio registrarMovimientoSistema(TipoMovimientoFolio tipo, Dinero monto, String descripcion, LocalDateTime en) {
        return append(tipo, monto, descripcion, en, null);
    }

    /// Compensa la última operación deshacible con su movimiento contrario.
    ///
    /// @throws ReglaNegocioExcepcion si no hay operaciones para deshacer
    public MovimientoFolio deshacerUltimo(LocalDateTime en) {
        if (pilaDeshacer.estaVacia()) {
            throw new ReglaNegocioExcepcion("No hay operaciones para deshacer en el folio " + numero);
        }
        MovimientoFolio ultimo = pilaDeshacer.desapilar();
        return append(ultimo.tipo().contrario(), ultimo.monto(),
                "Deshacer: " + ultimo.descripcion(), en, ultimo.numero());
    }

    /// Agrega un movimiento numerado al final de la lista; nunca modifica los anteriores.
    ///
    /// @throws ReglaNegocioExcepcion si `monto` es `null`, cero o negativo
    private MovimientoFolio append(TipoMovimientoFolio tipo, Dinero monto, String descripcion,
                              LocalDateTime en, Integer anulaMovimiento) {
        if (monto == null || !monto.esPositivo()) {
            throw new ReglaNegocioExcepcion("El valor del movimiento debe ser mayor que cero");
        }
        MovimientoFolio movimiento = new MovimientoFolio(movimientos.tamano() + 1, tipo, monto,
                descripcion == null || descripcion.isBlank() ? tipo.nombreMostrado() : descripcion.trim(),
                en, anulaMovimiento);
        movimientos.agregarUltimo(movimiento);
        return movimiento;
    }

    /// Valor que el huésped todavía debe (negativo significa que tiene saldo a favor).
    public Dinero saldo() {
        Dinero saldo = Dinero.CERO;
        for (MovimientoFolio movimiento : movimientos) {
            saldo = saldo.mas(movimiento.montoConSigno());
        }
        return saldo;
    }

    /// Dinero neto recibido: pagos menos pagos revertidos menos reembolsos.
    public Dinero montoPagado() {
        Dinero pagado = Dinero.CERO;
        for (MovimientoFolio movimiento : movimientos) {
            pagado = switch (movimiento.tipo()) {
                case PAGO -> pagado.mas(movimiento.monto());
                case ANULACION_PAGO, REEMBOLSO -> pagado.menos(movimiento.monto());
                case CARGO, ANULACION_CARGO -> pagado;
            };
        }
        return pagado;
    }

    /// Total cargado neto: cargos menos cargos revertidos.
    public Dinero montoCargado() {
        Dinero cargado = Dinero.CERO;
        for (MovimientoFolio movimiento : movimientos) {
            cargado = switch (movimiento.tipo()) {
                case CARGO -> cargado.mas(movimiento.monto());
                case ANULACION_CARGO -> cargado.menos(movimiento.monto());
                case PAGO, ANULACION_PAGO, REEMBOLSO -> cargado;
            };
        }
        return cargado;
    }

    /// Indica si hay alguna operación para deshacer.
    public boolean puedeDeshacer() {
        return !pilaDeshacer.estaVacia();
    }

    /// Operación que se desharía a continuación (cima de la pila), o `null`.
    public MovimientoFolio proximoDeshacible() {
        return pilaDeshacer.estaVacia() ? null : pilaDeshacer.ver();
    }

    /// Número del folio.
    public String obtenerNumero() {
        return numero;
    }

    /// Todos los movimientos en orden de registro.
    public ListaDoblementeEnlazada<MovimientoFolio> obtenerMovimientos() {
        return ListaDoblementeEnlazada.copiarDe(movimientos);
    }
}
