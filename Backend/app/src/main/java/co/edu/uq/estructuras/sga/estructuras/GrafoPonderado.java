package co.edu.uq.estructuras.sga.estructuras;

import java.util.Comparator;
import java.util.Iterator;

/**
 * Grafo no dirigido y ponderado, representado mediante listas de adyacencia.
 *
 * <p>Las aristas pueden cerrarse temporalmente sin eliminarse del grafo. Los
 * recorridos y los algoritmos de caminos ignoran las aristas cerradas, mientras
 * que {@link #aristas()} las conserva para permitir su consulta y reapertura.
 *
 * @param <T> tipo de los vértices; debe implementar correctamente
 *            {@link Object#equals(Object)} y {@link Object#hashCode()}
 */
public class GrafoPonderado<T> implements Iterable<T> {

    /**
     * Arista no dirigida del grafo.
     *
     * @param <T> tipo de los vértices que conecta la arista
     */
    public static final class Arista<T> {
        /** Un extremo de la arista. */
        private final T desde;
        /** El otro extremo de la arista. */
        private final T hasta;
        /** Peso no negativo de la arista. */
        private final double peso;
        /** Indica si los algoritmos de recorrido pueden utilizar la arista. */
        private boolean abierto = true;

        /**
         * Crea una arista abierta entre dos vértices.
         *
         * @param desde primer extremo
         * @param hasta segundo extremo
         * @param peso peso de la arista
         */
        private Arista(T desde, T hasta, double peso) {
            this.desde = desde;
            this.hasta = hasta;
            this.peso = peso;
        }

        /**
         * Devuelve el primer extremo de la arista.
         *
         * @return vértice almacenado como origen
         */
        public T desde() {
            return desde;
        }

        /**
         * Devuelve el segundo extremo de la arista.
         *
         * @return vértice almacenado como destino
         */
        public T hasta() {
            return hasta;
        }

        /**
         * Devuelve el peso de la arista.
         *
         * @return peso asociado a la arista
         */
        public double peso() {
            return peso;
        }

        /**
         * Indica si la arista está disponible para recorridos y caminos.
         *
         * @return {@code true} si la arista está abierta
         */
        public boolean estaAbierto() {
            return abierto;
        }

        /**
         * Obtiene el extremo opuesto al vértice indicado.
         *
         * @param vertice extremo conocido
         * @return el otro extremo de la arista
         */
        public T otro(T vertice) {
            return desde.equals(vertice) ? hasta : desde;
        }

        /**
         * Comprueba si la arista conecta los dos vértices, en cualquier sentido.
         *
         * @param a primer vértice
         * @param b segundo vértice
         * @return {@code true} si ambos vértices son los extremos de la arista
         */
        public boolean une(T a, T b) {
            return (desde.equals(a) && hasta.equals(b)) || (desde.equals(b) && hasta.equals(a));
        }
    }

    /**
     * Resultado de una consulta de camino más corto.
     *
     * @param vertices vértices del camino, desde el origen hasta el destino
     * @param pesoTotal suma de los pesos de las aristas del camino
     * @param <T> tipo de los vértices
     */
    public record Camino<T>(ListaDoblementeEnlazada<T> vertices, double pesoTotal) {

        /**
         * Indica si el destino es alcanzable desde el origen.
         *
         * @return {@code true} cuando el camino contiene al menos un vértice
         */
        public boolean esAlcanzable() {
            return !vertices.estaVacia();
        }
    }

    /** Entrada de la cola de prioridad utilizada por Dijkstra. */
    private record EntradaDistancia<T>(T vertice, double distancia) {
    }

    /** Aristas incidentes a cada vértice. */
    private final MapaHash<T, ListaDoblementeEnlazada<Arista<T>>> adyacencia = new MapaHash<>();
    /** Vértices en orden de inserción para producir recorridos deterministas. */
    private final ListaDoblementeEnlazada<T> ordenVertices = new ListaDoblementeEnlazada<>();
    /** Aristas del grafo, incluidas las que están cerradas. */
    private final ListaDoblementeEnlazada<Arista<T>> aristas = new ListaDoblementeEnlazada<>();

    /**
     * Agrega un vértice si todavía no forma parte del grafo.
     *
     * @param vertice vértice que se agregará
     * @return {@code true} si se agregó; {@code false} si ya existía
     */
    public boolean agregarVertice(T vertice) {
        if (adyacencia.contieneLlave(vertice)) {
            return false;
        }
        adyacencia.poner(vertice, new ListaDoblementeEnlazada<>());
        ordenVertices.agregarUltimo(vertice);
        return true;
    }

    /**
     * Comprueba si el grafo contiene un vértice.
     *
     * @param vertice vértice que se buscará
     * @return {@code true} si el vértice existe
     */
    public boolean contieneVertice(T vertice) {
        return adyacencia.contieneLlave(vertice);
    }

    /**
     * Agrega una arista no dirigida y crea sus extremos si aún no existen.
     *
     * @param desde primer extremo
     * @param hasta segundo extremo
     * @param peso peso no negativo de la arista
     * @throws IllegalArgumentException si {@code peso} es negativo
     */
    public void agregarArista(T desde, T hasta, double peso) {
        if (peso < 0) {
            throw new IllegalArgumentException("El peso de una arista no puede ser negativo");
        }
        agregarVertice(desde);
        agregarVertice(hasta);
        Arista<T> arista = new Arista<>(desde, hasta, peso);
        adyacencia.obtener(desde).agregarUltimo(arista);
        adyacencia.obtener(hasta).agregarUltimo(arista);
        aristas.agregarUltimo(arista);
    }

    /**
     * Busca una arista que conecte dos vértices.
     *
     * @param a primer vértice
     * @param b segundo vértice
     * @return la primera arista que los conecta, o {@code null} si no existe
     */
    public Arista<T> buscarArista(T a, T b) {
        ListaDoblementeEnlazada<Arista<T>> incidente = adyacencia.obtener(a);
        return incidente == null ? null : incidente.buscar(arista -> arista.une(a, b));
    }

    /**
     * Cambia el estado operativo de una arista sin eliminarla.
     *
     * @param a primer extremo de la arista
     * @param b segundo extremo de la arista
     * @param abierto nuevo estado operativo
     * @return {@code true} si la arista existe y se actualizó
     */
    public boolean establecerAbiertoArista(T a, T b, boolean abierto) {
        Arista<T> arista = buscarArista(a, b);
        if (arista == null) {
            return false;
        }
        arista.abierto = abierto;
        return true;
    }

    /**
     * Obtiene los vecinos conectados mediante aristas abiertas.
     *
     * @param vertice vértice cuyos vecinos se consultarán
     * @return copia de los vecinos en el orden de sus aristas incidentes
     */
    public ListaDoblementeEnlazada<T> vecinos(T vertice) {
        ListaDoblementeEnlazada<T> resultado = new ListaDoblementeEnlazada<>();
        for (Arista<T> arista : aristasIncidente(vertice)) {
            if (arista.abierto) {
                resultado.agregarUltimo(arista.otro(vertice));
            }
        }
        return resultado;
    }

    /**
     * Obtiene los vértices en orden de inserción.
     *
     * @return copia independiente de la lista de vértices
     */
    public ListaDoblementeEnlazada<T> vertices() {
        return ListaDoblementeEnlazada.copiarDe(ordenVertices);
    }

    /**
     * Obtiene todas las aristas sin duplicar la representación no dirigida.
     *
     * @return copia independiente de las aristas, abiertas o cerradas
     */
    public ListaDoblementeEnlazada<Arista<T>> aristas() {
        return ListaDoblementeEnlazada.copiarDe(aristas);
    }

    /**
     * Cuenta los vértices del grafo.
     *
     * @return cantidad de vértices
     */
    public int cantidadVertices() {
        return ordenVertices.tamano();
    }

    /**
     * Cuenta las aristas del grafo.
     *
     * @return cantidad de aristas
     */
    public int cantidadAristas() {
        return aristas.tamano();
    }

    /**
     * Recorre el grafo en anchura usando únicamente aristas abiertas.
     *
     * @param inicio vértice desde el que comienza el recorrido
     * @return vértices visitados en el orden del recorrido
     * @throws IllegalArgumentException si {@code inicio} no pertenece al grafo
     */
    public ListaDoblementeEnlazada<T> anchuraPrimero(T inicio) {
        exigirVertice(inicio);
        ListaDoblementeEnlazada<T> orden = new ListaDoblementeEnlazada<>();
        ConjuntoHash<T> visitados = new ConjuntoHash<>();
        ColaEnlazada<T> pendiente = new ColaEnlazada<>();
        visitados.agregar(inicio);
        pendiente.encolar(inicio);
        while (!pendiente.estaVacia()) {
            T actual = pendiente.desencolar();
            orden.agregarUltimo(actual);
            for (T vecino : vecinos(actual)) {
                if (visitados.agregar(vecino)) {
                    pendiente.encolar(vecino);
                }
            }
        }
        return orden;
    }

    /**
     * Recorre el grafo en profundidad mediante DFS recursivo.
     *
     * @param inicio vértice desde el que comienza el recorrido
     * @return vértices visitados en el orden del recorrido
     * @throws IllegalArgumentException si {@code inicio} no pertenece al grafo
     */
    public ListaDoblementeEnlazada<T> profundidadPrimero(T inicio) {
        exigirVertice(inicio);
        ListaDoblementeEnlazada<T> orden = new ListaDoblementeEnlazada<>();
        profundidadPrimero(inicio, new ConjuntoHash<>(), orden);
        return orden;
    }

    /**
     * Visita recursivamente los vecinos abiertos que aún no se han procesado.
     *
     * @param actual vértice que se está procesando
     * @param visitados conjunto de vértices ya visitados
     * @param orden lista donde se acumula el orden de visita
     */
    private void profundidadPrimero(T actual, ConjuntoHash<T> visitados, ListaDoblementeEnlazada<T> orden) {
        visitados.agregar(actual);
        orden.agregarUltimo(actual);
        for (T vecino : vecinos(actual)) {
            if (!visitados.contiene(vecino)) {
                profundidadPrimero(vecino, visitados, orden);
            }
        }
    }

    /**
     * Calcula las componentes conexas considerando solo aristas abiertas.
     *
     * @return componentes en orden de aparición de sus vértices; cada
     *         componente conserva el orden producido por DFS
     */
    public ListaDoblementeEnlazada<ListaDoblementeEnlazada<T>> componentesConexos() {
        ListaDoblementeEnlazada<ListaDoblementeEnlazada<T>> componentes = new ListaDoblementeEnlazada<>();
        ConjuntoHash<T> visitados = new ConjuntoHash<>();
        for (T vertice : ordenVertices) {
            if (!visitados.contiene(vertice)) {
                ListaDoblementeEnlazada<T> componente = new ListaDoblementeEnlazada<>();
                profundidadPrimero(vertice, visitados, componente);
                componentes.agregarUltimo(componente);
            }
        }
        return componentes;
    }

    /**
     * Obtiene los vértices que no son alcanzables desde un origen.
     *
     * @param inicio vértice desde el que se comprueba la alcanzabilidad
     * @return vértices no alcanzables, en orden de inserción
     * @throws IllegalArgumentException si {@code inicio} no pertenece al grafo
     */
    public ListaDoblementeEnlazada<T> inalcanzablesDesde(T inicio) {
        ConjuntoHash<T> alcanzable = new ConjuntoHash<>();
        for (T vertice : anchuraPrimero(inicio)) {
            alcanzable.agregar(vertice);
        }
        return ordenVertices.filtrar(vertice -> !alcanzable.contiene(vertice));
    }

    /**
     * Busca el camino de menor peso mediante el algoritmo de Dijkstra.
     *
     * @param origen vértice inicial
     * @param destino vértice final
     * @return camino encontrado y su peso total; si no existe, camino vacío y
     *         {@link Double#POSITIVE_INFINITY}
     * @throws IllegalArgumentException si alguno de los vértices no pertenece
     *         al grafo
     */
    public Camino<T> caminoMasCorto(T origen, T destino) {
        exigirVertice(origen);
        exigirVertice(destino);
        MapaHash<T, Double> distancias = new MapaHash<>();
        MapaHash<T, T> anterior = new MapaHash<>();
        ejecutarDijkstra(origen, distancias, anterior);

        Double total = distancias.obtener(destino);
        if (total == null) {
            return new Camino<>(new ListaDoblementeEnlazada<>(), Double.POSITIVE_INFINITY);
        }
        ListaDoblementeEnlazada<T> camino = new ListaDoblementeEnlazada<>();
        for (T paso = destino; paso != null; paso = anterior.obtener(paso)) {
            camino.agregarPrimero(paso);
        }
        return new Camino<>(camino, total);
    }

    /**
     * Calcula la distancia mínima desde un origen hasta cada vértice alcanzable.
     *
     * @param origen vértice inicial
     * @return mapa con una entrada por vértice alcanzable, incluido el origen
     * @throws IllegalArgumentException si {@code origen} no pertenece al grafo
     */
    public MapaHash<T, Double> distanciasDesde(T origen) {
        exigirVertice(origen);
        MapaHash<T, Double> distancias = new MapaHash<>();
        ejecutarDijkstra(origen, distancias, new MapaHash<>());
        return distancias;
    }

    /**
     * Ejecuta Dijkstra y almacena distancias y predecesores en los mapas
     * recibidos. Las aristas cerradas se omiten.
     *
     * @param origen vértice inicial
     * @param distancias mapa donde se escribirán las distancias mínimas
     * @param anterior mapa donde se escribirán los predecesores
     */
    private void ejecutarDijkstra(T origen, MapaHash<T, Double> distancias, MapaHash<T, T> anterior) {
        ColaPrioridadMonticulo<EntradaDistancia<T>> frontera =
                new ColaPrioridadMonticulo<>(Comparator.comparingDouble(EntradaDistancia::distancia));
        ConjuntoHash<T> asentado = new ConjuntoHash<>();
        distancias.poner(origen, 0.0);
        frontera.ofrecer(new EntradaDistancia<>(origen, 0.0));

        while (!frontera.estaVacia()) {
            EntradaDistancia<T> cercano = frontera.sacar();
            if (!asentado.agregar(cercano.vertice())) {
                continue; // entrada vieja: ya se fijó una distancia más corta
            }
            for (Arista<T> arista : aristasIncidente(cercano.vertice())) {
                if (!arista.abierto) {
                    continue;
                }
                T vecino = arista.otro(cercano.vertice());
                double candidato = cercano.distancia() + arista.peso;
                Double conocido = distancias.obtener(vecino);
                if (conocido == null || candidato < conocido) {
                    distancias.poner(vecino, candidato);
                    anterior.poner(vecino, cercano.vertice());
                    frontera.ofrecer(new EntradaDistancia<>(vecino, candidato));
                }
            }
        }
    }

    /**
     * Devuelve un iterador sobre los vértices en orden de inserción.
     *
     * @return iterador de los vértices del grafo
     */
    @Override
    public Iterator<T> iterator() {
        return ordenVertices.iterator();
    }

    /**
     * Obtiene las aristas incidentes a un vértice.
     *
     * @param vertice vértice cuyas aristas se consultarán
     * @return lista de aristas incidentes o una lista vacía si el vértice no
     *         existe
     */
    private ListaDoblementeEnlazada<Arista<T>> aristasIncidente(T vertice) {
        ListaDoblementeEnlazada<Arista<T>> incidente = adyacencia.obtener(vertice);
        return incidente == null ? new ListaDoblementeEnlazada<>() : incidente;
    }

    /**
     * Valida que un vértice pertenezca al grafo.
     *
     * @param vertice vértice que se validará
     * @throws IllegalArgumentException si el vértice no existe
     */
    private void exigirVertice(T vertice) {
        if (!adyacencia.contieneLlave(vertice)) {
            throw new IllegalArgumentException("El vértice '" + vertice + "' no existe en el grafo");
        }
    }
}
