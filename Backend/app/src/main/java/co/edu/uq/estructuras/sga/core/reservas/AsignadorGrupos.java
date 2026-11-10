package co.edu.uq.estructuras.sga.core.reservas;

import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.estructuras.PilaEnlazada;
import co.edu.uq.estructuras.sga.excepciones.CapacidadExcedidaExcepcion;

/// Encuentra, con backtracking recursivo, la combinación de apartamentos disponibles que aloja a
/// todo un grupo usando la menor cantidad de apartamentos. Entre las combinaciones de ese tamaño
/// se queda con la que desperdicia menos camas. No guarda estado: cada llamada tiene el suyo.
///
/// Cómo funciona:
/// - **Profundización iterativa:** prueba primero con 1 apartamento, luego con 2, y así sucesivamente;
///   el primer tamaño que tiene solución es el mínimo.
/// - **Backtracking:** para cada tamaño, elige un apartamento (lo apila en `elegidos`), sigue buscando
///   recursivamente con los que quedan y, al volver, lo saca (lo desapila) para probar el siguiente.
/// - **Poda:** los candidatos se ordenan de mayor a menor capacidad. Si ni siquiera los apartamentos
///   más grandes que quedan alcanzan para las personas restantes, se corta esa rama y las siguientes,
///   porque solo pueden ser más pequeñas. También se detiene al encontrar un encaje perfecto (0 camas sobrantes).
public class AsignadorGrupos {

    /// Estado mutable de una búsqueda, para que dos llamadas concurrentes nunca lo compartan.
    private static final class Busqueda {
        /// Apartamentos libres de mayor a menor capacidad.
        private final Apartamento[] candidatos;
        /// Combinación que se está armando (se apila al elegir y se desapila al retroceder).
        private final PilaEnlazada<Apartamento> elegidos = new PilaEnlazada<>();
        /// Mejor combinación encontrada hasta ahora.
        private ListaDoblementeEnlazada<Apartamento> mejor;
        /// Camas sobrantes de la mejor combinación.
        private int mejorDesperdicio = Integer.MAX_VALUE;

        /// Crea una búsqueda vacía sobre los candidatos dados.
        ///
        /// @param candidatos apartamentos libres ordenados de mayor a menor capacidad
        private Busqueda(Apartamento[] candidatos) {
            this.candidatos = candidatos;
        }
    }

    /// Devuelve el menor conjunto de apartamentos donde cabe todo el grupo; entre los conjuntos de ese
    /// tamaño, el que deja menos camas sobrantes. No modifica `disponibles`: trabaja sobre una copia ordenada.
    ///
    /// @param disponibles apartamentos libres entre los que se puede elegir
    /// @param tamanoGrupo número de personas del grupo
    /// @return apartamentos elegidos, del último elegido al primero (es decir, de menor a mayor capacidad)
    /// @throws CapacidadExcedidaExcepcion si `tamanoGrupo` es menor que 1 o mayor que la capacidad total de `disponibles`
    public ListaDoblementeEnlazada<Apartamento> asignar(ListaDoblementeEnlazada<Apartamento> disponibles, int tamanoGrupo) {
        // Primero los apartamentos más grandes: se encuentran buenas soluciones temprano y la poda es más fuerte.
        Apartamento[] candidatos = disponibles.ordenada((a, b) -> Integer.compare(b.obtenerCapacidad(), a.obtenerCapacidad()))
                .aArreglo(Apartamento[]::new);
        int capacidadTotal = 0;
        for (Apartamento apartamento : candidatos) {
            capacidadTotal += apartamento.obtenerCapacidad();
        }
        if (tamanoGrupo < 1 || capacidadTotal < tamanoGrupo) {
            throw new CapacidadExcedidaExcepcion("Los apartamentos disponibles", tamanoGrupo, capacidadTotal);
        }
        // Profundización iterativa: se prueba con 1 apartamento, luego 2... el primer tamaño con solución es el mínimo.
        for (int tamano = 1; tamano <= candidatos.length; tamano++) {
            Busqueda buscar = new Busqueda(candidatos);
            explorar(buscar, 0, tamano, tamanoGrupo);
            if (buscar.mejor != null) {
                return buscar.mejor;
            }
        }
        // No debería llegar aquí: si la capacidad total alcanza, usar todos los apartamentos es una solución.
        throw new CapacidadExcedidaExcepcion("Los apartamentos disponibles", tamanoGrupo, capacidadTotal);
    }

    /// Paso recursivo del backtracking: elige `franjas` apartamentos más, desde la posición `inicio`,
    /// para que quepan las `restantes` personas. Caso base: `franjas == 0`; si ya caben todos y sobran
    /// menos camas que en la mejor combinación, guarda una copia de `elegidos` como la nueva mejor.
    ///
    /// @param buscar estado de la búsqueda actual
    /// @param inicio primera posición de `candidatos` que se puede elegir (evita repetir combinaciones)
    /// @param franjas cuántos apartamentos faltan por elegir
    /// @param restantes personas que todavía no tienen cama (negativo si ya sobran camas)
    private void explorar(Busqueda buscar, int inicio, int franjas, int restantes) {
        if (franjas == 0) {
            int desperdicio = -restantes;
            if (restantes <= 0 && desperdicio < buscar.mejorDesperdicio) {
                buscar.mejorDesperdicio = desperdicio;
                buscar.mejor = ListaDoblementeEnlazada.copiarDe(buscar.elegidos);
            }
            return;
        }
        for (int i = inicio; i <= buscar.candidatos.length - franjas; i++) {
            // Poda: aun tomando los `franjas` apartamentos más grandes desde aquí, el grupo no cabría.
            if (maximoCamas(buscar.candidatos, i, franjas) < restantes) {
                return;
            }
            Apartamento apartamento = buscar.candidatos[i];
            buscar.elegidos.apilar(apartamento);
            explorar(buscar, i + 1, franjas - 1, restantes - apartamento.obtenerCapacidad());
            buscar.elegidos.desapilar(); // retroceso (backtrack)
            if (buscar.mejorDesperdicio == 0) {
                return; // encaje perfecto, no existe nada mejor
            }
        }
    }

    /// Camas que suman los `franjas` apartamentos que siguen desde `desde`. Como los candidatos están
    /// ordenados por capacidad descendente, son los más grandes que quedan.
    ///
    /// @param candidatos apartamentos ordenados de mayor a menor capacidad
    /// @param desde posición desde la que se cuenta
    /// @param franjas cuántos apartamentos se suman
    /// @return capacidad total de esos apartamentos (o de los que haya, si quedan menos)
    private static int maximoCamas(Apartamento[] candidatos, int desde, int franjas) {
        int camas = 0;
        for (int i = desde; i < desde + franjas && i < candidatos.length; i++) {
            camas += candidatos[i].obtenerCapacidad();
        }
        return camas;
    }
}
