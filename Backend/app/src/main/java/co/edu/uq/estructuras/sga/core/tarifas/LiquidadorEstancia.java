package co.edu.uq.estructuras.sga.core.tarifas;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.core.reservas.Ocupante;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;

import java.time.LocalDate;
import java.util.Iterator;

/// Liquida una estancia noche por noche. Cada noche usa la temporada en la que cae, así una
/// estancia que cruza temporadas se cobra bien. Los menores de `Ocupante.EDAD_LIMITE_GRATIS` (6) años
/// no pagan: se cuentan como huéspedes gratis de esa noche.
public class LiquidadorEstancia {

    /// Calendario que indica la temporada de cada noche.
    private final CalendarioTemporadas calendario;

    /// Crea un liquidador que usa el calendario dado.
    ///
    /// @param calendario calendario que dice la temporada de cada noche
    public LiquidadorEstancia(CalendarioTemporadas calendario) {
        this.calendario = calendario;
    }

    /// Cotiza la estancia noche por noche para los ocupantes dados.
    ///
    /// @param apartamento apartamento cuyas tarifas se aplican
    /// @param estancia fechas de entrada y salida; se cobra cada noche desde la entrada hasta el día anterior a la salida
    /// @param ocupantes personas que se alojan; se recorren en cada noche para ver quién paga
    /// @return cotización con una [CobroNoche] por noche, en orden, y el total
    public Cotizacion cotizacion(Apartamento apartamento, Estancia estancia, Iterable<Ocupante> ocupantes) {
        ListaDoblementeEnlazada<CobroNoche> desglose = new ListaDoblementeEnlazada<>();
        Dinero total = liquidar(estancia.iterator(), apartamento, ocupantes, desglose);
        return new Cotizacion(estancia, desglose, total);
    }

    /// Liquidación recursiva: el valor de las noches restantes es el valor de la siguiente noche
    /// más el valor del resto. Caso base: no quedan noches → $0.
    ///
    /// Cada llamada agrega su noche a `desglose` antes de la llamada recursiva, así el desglose
    /// queda en el mismo orden en que el iterador de [Estancia] entrega las noches.
    ///
    /// @param noches iterador con las noches que faltan por liquidar
    /// @param apartamento apartamento cuyas tarifas se aplican
    /// @param ocupantes personas que se alojan
    /// @param desglose lista donde se agrega el cobro de cada noche
    /// @return suma de los subtotales de las noches que quedaban en `noches`
    private Dinero liquidar(Iterator<LocalDate> noches, Apartamento apartamento,
                            Iterable<Ocupante> ocupantes, ListaDoblementeEnlazada<CobroNoche> desglose) {
        if (!noches.hasNext()) {
            return Dinero.CERO;
        }
        CobroNoche cargo = liquidarNoche(noches.next(), apartamento, ocupantes);
        desglose.agregarUltimo(cargo);
        return cargo.subtotal().mas(liquidar(noches, apartamento, ocupantes, desglose));
    }

    /// Liquida una noche: tarifa de la temporada × ocupantes que pagan esa noche (los menores de 6 no pagan).
    ///
    /// @param noche noche que se liquida
    /// @param apartamento apartamento cuyas tarifas se aplican
    /// @param ocupantes personas que se alojan
    /// @return cobro de la noche con su temporada, tarifa por persona, huéspedes que pagan, gratis y subtotal
    private CobroNoche liquidarNoche(LocalDate noche, Apartamento apartamento, Iterable<Ocupante> ocupantes) {
        int pagan = 0;
        int libre = 0;
        for (Ocupante ocupante : ocupantes) {
            if (ocupante.pagaEn(noche)) {
                pagan++;
            } else {
                libre++;
            }
        }
        Temporada temporada = calendario.temporadaDe(noche);
        Dinero tarifa = apartamento.tarifaPara(temporada);
        return new CobroNoche(noche, temporada, tarifa, pagan, libre, tarifa.veces(pagan));
    }

    /// Precio estimado cuando solo se conoce el número de huéspedes (todos pagan). Recorre las noches
    /// con un for-each, sin recursión, y no arma desglose.
    ///
    /// @param apartamento apartamento cuyas tarifas se aplican
    /// @param estancia fechas de entrada y salida
    /// @param huespedes cantidad de huéspedes; se supone que todos pagan
    /// @return suma, noche por noche, de la tarifa de la temporada × `huespedes`
    public Dinero estimado(Apartamento apartamento, Estancia estancia, int huespedes) {
        Dinero total = Dinero.CERO;
        for (LocalDate noche : estancia) {
            total = total.mas(apartamento.tarifaPara(calendario.temporadaDe(noche)).veces(huespedes));
        }
        return total;
    }
}
