package co.edu.uq.estructuras.sga.estructuras;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GrafoPonderadoTest {

    private GrafoPonderado<String> grafo;

    /// A-B 1, B-C 2, A-C 5, C-D 1 y E aislado.
    @BeforeEach
    void preparar() {
        grafo = new GrafoPonderado<>();
        for (String v : new String[] {"A", "B", "C", "D", "E"}) {
            grafo.agregarVertice(v);
        }
        grafo.agregarArista("A", "B", 1);
        grafo.agregarArista("B", "C", 2);
        grafo.agregarArista("A", "C", 5);
        grafo.agregarArista("C", "D", 1);
    }

    @Test
    @DisplayName("Dijkstra elige el camino de menor peso, no el de menos saltos")
    void dijkstra() {
        GrafoPonderado.Camino<String> camino = grafo.caminoMasCorto("A", "D");
        assertArrayEquals(new String[] {"A", "B", "C", "D"}, camino.vertices().aArreglo(String[]::new));
        assertEquals(4, camino.pesoTotal());
    }

    @Test
    @DisplayName("un vértice aislado no es alcanzable")
    void inalcanzable() {
        assertFalse(grafo.caminoMasCorto("A", "E").esAlcanzable());
        assertArrayEquals(new String[] {"E"}, grafo.inalcanzablesDesde("A").aArreglo(String[]::new));
    }

    @Test
    @DisplayName("cerrar una arista cambia el camino y los componentes")
    void cerrarArista() {
        grafo.establecerAbiertoArista("B", "C", false);
        assertEquals(6, grafo.caminoMasCorto("A", "D").pesoTotal());
        grafo.establecerAbiertoArista("A", "C", false);
        assertEquals(3, grafo.componentesConexos().tamano()); // {A,B} {C,D} {E}
    }

    @Test
    @DisplayName("BFS y DFS visitan todos los alcanzables una sola vez")
    void recorridos() {
        assertEquals(4, grafo.anchuraPrimero("A").tamano());
        assertEquals(4, grafo.profundidadPrimero("A").tamano());
        assertEquals("A", grafo.anchuraPrimero("A").obtener(0));
    }

    @Test
    @DisplayName("distanciasDesde entrega la distancia mínima a cada alcanzable")
    void distancias() {
        MapaHash<String, Double> d = grafo.distanciasDesde("A");
        assertEquals(3.0, d.obtener("C"));
        assertNull(d.obtener("E"));
    }
}
