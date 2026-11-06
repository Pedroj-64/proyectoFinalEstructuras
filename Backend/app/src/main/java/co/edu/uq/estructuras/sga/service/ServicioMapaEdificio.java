package co.edu.uq.estructuras.sga.service;


import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import co.edu.uq.estructuras.sga.core.edificio.NodoEdificio;
import co.edu.uq.estructuras.sga.core.edificio.TipoNodo;
import co.edu.uq.estructuras.sga.estructuras.ConjuntoHash;
import co.edu.uq.estructuras.sga.estructuras.GrafoPonderado;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.estructuras.MapaHash;
import co.edu.uq.estructuras.sga.excepciones.NoEncontradoExcepcion;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.service.Util.EscenarioReiniciadoEvento;

/// Mapa del edificio como grafo ponderado: rutas de preparación (Dijkstra), análisis de
/// conectividad (BFS/DFS) e impacto de cerrar un pasillo.
@Service
public class ServicioMapaEdificio {

    /// Tramo de una ruta entre dos paradas consecutivas.
    public record TramoRuta(String desde, String hasta, ListaDoblementeEnlazada<String> camino, double metros) {
    }

    /// Ruta completa de preparación que empieza en recepción.
    public record RutaPreparacion(String inicio, ListaDoblementeEnlazada<String> paradas, ListaDoblementeEnlazada<TramoRuta> tramos,
                                   ListaDoblementeEnlazada<String> caminoCompleto, double metrosTotales,
                                   ListaDoblementeEnlazada<String> inalcanzables) {
    }

    /// Apartamentos de un bloque, en el orden en que los descubre un BFS desde recepción.
    public record GrupoBloque(String bloque, ListaDoblementeEnlazada<String> codigosApartamentos) {
    }

    /// Grafo del edificio; los vértices son los ids de los nodos y los pesos son metros.
    private GrafoPonderado<String> grafo = new GrafoPonderado<>();
    /// Datos de cada nodo (tipo, etiqueta, bloque, coordenadas) por id.
    private final MapaHash<String, NodoEdificio> nodos = new MapaHash<>();

    /// Observer: limpia el grafo antes de cargar un escenario nuevo.
    @EventListener
    public void alReiniciarEscenario(EscenarioReiniciadoEvento evento) {
        grafo = new GrafoPonderado<>();
        nodos.vaciar();
    }

    /// Agrega un punto del mapa como vértice del grafo.
    public void agregarNodo(NodoEdificio nodo) {
        if (nodos.contieneLlave(nodo.id())) {
            throw new ReglaNegocioExcepcion("Ya existe un punto del mapa con id " + nodo.id());
        }
        nodos.poner(nodo.id(), nodo);
        grafo.agregarVertice(nodo.id());
    }

    /// Agrega un pasillo como arista con su longitud en metros.
    public void agregarPasillo(String desde, String hasta, double metros) {
        exigirNodo(desde);
        exigirNodo(hasta);
        if (desde.equals(hasta)) {
            throw new ReglaNegocioExcepcion("Un pasillo debe unir dos puntos distintos");
        }
        if (grafo.buscarArista(desde, hasta) != null) {
            throw new ReglaNegocioExcepcion("Ya existe un pasillo entre " + desde + " y " + hasta);
        }
        if (metros <= 0) {
            throw new ReglaNegocioExcepcion("La longitud del pasillo debe ser mayor que cero");
        }
        grafo.agregarArista(desde, hasta, metros);
    }

    /// Abre o cierra un pasillo existente.
    public void establecerAbiertoPasillo(String desde, String hasta, boolean abierto) {
        if (!grafo.establecerAbiertoArista(desde, hasta, abierto)) {
            throw new NoEncontradoExcepcion("El pasillo", desde + " - " + hasta);
        }
    }

    /// Puntos del mapa en orden de inserción.
    public ListaDoblementeEnlazada<NodoEdificio> nodos() {
        ListaDoblementeEnlazada<NodoEdificio> resultado = new ListaDoblementeEnlazada<>();
        for (String id : grafo.vertices()) {
            resultado.agregarUltimo(nodos.obtener(id));
        }
        return resultado;
    }

    /// Pasillos del mapa (abiertos y cerrados).
    public ListaDoblementeEnlazada<GrafoPonderado.Arista<String>> pasillos() {
        return grafo.aristas();
    }

    /// Punto del mapa por id o [NoEncontradoExcepcion].
    public NodoEdificio nodo(String id) {
        return exigirNodo(id);
    }

    /// Camino más corto entre dos puntos con Dijkstra.
    public GrafoPonderado.Camino<String> caminoMasCorto(String desde, String hasta) {
        exigirNodo(desde);
        exigirNodo(hasta);
        return grafo.caminoMasCorto(desde, hasta);
    }

    /// Ruta que visita los grupos en orden (primero el de prioridad 1). Dentro de cada grupo la
    /// siguiente parada es siempre la más cercana (vecino más cercano), medida con Dijkstra.
    public RutaPreparacion rutaPor(ListaDoblementeEnlazada<ListaDoblementeEnlazada<String>> gruposPrioridad) {
        String inicio = idRecepcion();
        ListaDoblementeEnlazada<String> paradas = new ListaDoblementeEnlazada<>();
        ListaDoblementeEnlazada<TramoRuta> tramos = new ListaDoblementeEnlazada<>();
        ListaDoblementeEnlazada<String> caminoCompleto = ListaDoblementeEnlazada.de(inicio);
        ListaDoblementeEnlazada<String> inalcanzables = new ListaDoblementeEnlazada<>();
        double total = 0;
        String posicion = inicio;

        for (ListaDoblementeEnlazada<String> grupo : gruposPrioridad) {
            ListaDoblementeEnlazada<String> restantes = grupo.filtrar(nodos::contieneLlave);
            while (!restantes.estaVacia()) {
                MapaHash<String, Double> distancias = grafo.distanciasDesde(posicion);
                String cercano = null;
                for (String candidato : restantes) {
                    Double distancia = distancias.obtener(candidato);
                    if (distancia != null && (cercano == null || distancia < distancias.obtener(cercano))) {
                        cercano = candidato;
                    }
                }
                if (cercano == null) {
                    inalcanzables.agregarTodos(restantes);
                    break;
                }
                GrafoPonderado.Camino<String> camino = grafo.caminoMasCorto(posicion, cercano);
                tramos.agregarUltimo(new TramoRuta(posicion, cercano, camino.vertices(), camino.pesoTotal()));
                ListaDoblementeEnlazada<String> pasos = ListaDoblementeEnlazada.copiarDe(camino.vertices());
                pasos.quitarPrimero(); // el primer vértice es la posición actual, que ya está en el camino
                caminoCompleto.agregarTodos(pasos);
                total += camino.pesoTotal();
                paradas.agregarUltimo(cercano);
                restantes.quitar(cercano);
                posicion = cercano;
            }
        }
        return new RutaPreparacion(inicio, paradas, tramos, caminoCompleto, total, inalcanzables);
    }

    /// Apartamentos a los que no se puede llegar desde recepción con los pasillos abiertos ahora.
    public ListaDoblementeEnlazada<String> apartamentosInalcanzables() {
        return grafo.inalcanzablesDesde(idRecepcion()).filtrar(this::esApartamento);
    }

    /// Lo que quedaría inalcanzable si se cerrara este pasillo (el grafo no se modifica).
    public ListaDoblementeEnlazada<String> impactoCierre(String desde, String hasta) {
        GrafoPonderado.Arista<String> arista = grafo.buscarArista(desde, hasta);
        if (arista == null) {
            throw new NoEncontradoExcepcion("El pasillo", desde + " - " + hasta);
        }
        boolean estabaAbierto = arista.estaAbierto();
        grafo.establecerAbiertoArista(desde, hasta, false);
        try {
            return apartamentosInalcanzables();
        } finally {
            grafo.establecerAbiertoArista(desde, hasta, estabaAbierto);
        }
    }

    /// Agrupa los apartamentos por bloque en el orden en que los descubre un BFS desde recepción.
    /// Los que el BFS no alcanza quedan en el grupo "Inalcanzables".
    public ListaDoblementeEnlazada<GrupoBloque> apartamentosPorBloque() {
        MapaHash<String, ListaDoblementeEnlazada<String>> porBloque = new MapaHash<>();
        ListaDoblementeEnlazada<String> ordenBloques = new ListaDoblementeEnlazada<>();
        ConjuntoHash<String> alcanzados = new ConjuntoHash<>();
        for (String id : grafo.anchuraPrimero(idRecepcion())) {
            alcanzados.agregar(id);
            NodoEdificio nodo = nodos.obtener(id);
            if (nodo.tipo() == TipoNodo.APARTAMENTO) {
                if (!porBloque.contieneLlave(nodo.bloque())) {
                    ordenBloques.agregarUltimo(nodo.bloque());
                }
                porBloque.calcularSiAusente(nodo.bloque(), ListaDoblementeEnlazada::new).agregarUltimo(id);
            }
        }
        ListaDoblementeEnlazada<GrupoBloque> grupos = new ListaDoblementeEnlazada<>();
        for (String bloque : ordenBloques) {
            grupos.agregarUltimo(new GrupoBloque(bloque, porBloque.obtener(bloque)));
        }
        ListaDoblementeEnlazada<String> aislados = grafo.vertices().filtrar(id -> esApartamento(id) && !alcanzados.contiene(id));
        if (!aislados.estaVacia()) {
            grupos.agregarUltimo(new GrupoBloque("Inalcanzables", aislados));
        }
        return grupos;
    }

    /// Recorrido BFS desde un punto.
    public ListaDoblementeEnlazada<String> anchuraPrimeroDesde(String inicio) {
        exigirNodo(inicio);
        return grafo.anchuraPrimero(inicio);
    }

    /// Recorrido DFS recursivo desde un punto.
    public ListaDoblementeEnlazada<String> profundidadPrimeroDesde(String inicio) {
        exigirNodo(inicio);
        return grafo.profundidadPrimero(inicio);
    }

    /// Componentes conexos del edificio con los pasillos abiertos.
    public ListaDoblementeEnlazada<ListaDoblementeEnlazada<String>> componentesConexos() {
        return grafo.componentesConexos();
    }

    /// Id del nodo de recepción, punto de partida de las rutas.
    public String idRecepcion() {
        for (String id : grafo.vertices()) {
            if (nodos.obtener(id).tipo() == TipoNodo.RECEPCION) {
                return id;
            }
        }
        throw new ReglaNegocioExcepcion("El mapa del edificio no tiene recepción");
    }

    /// Indica si ya se cargó algún punto del mapa.
    public boolean tieneNodos() {
        return grafo.cantidadVertices() > 0;
    }

    /// Indica si el nodo del mapa corresponde a un apartamento (y no a recepción, escaleras o aseo).
    private boolean esApartamento(String id) {
        NodoEdificio nodo = nodos.obtener(id);
        return nodo != null && nodo.tipo() == TipoNodo.APARTAMENTO;
    }

    /// Busca un nodo del mapa o lanza [NoEncontradoExcepcion] si no existe.
    private NodoEdificio exigirNodo(String id) {
        NodoEdificio nodo = nodos.obtener(id);
        if (nodo == null) {
            throw new NoEncontradoExcepcion("El punto del mapa", id);
        }
        return nodo;
    }
}
