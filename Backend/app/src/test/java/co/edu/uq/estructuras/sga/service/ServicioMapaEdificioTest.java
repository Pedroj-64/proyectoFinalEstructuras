package co.edu.uq.estructuras.sga.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.NoEncontradoExcepcion;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.core.edificio.NodoEdificio;
import co.edu.uq.estructuras.sga.core.edificio.TipoNodo;

class ServicioMapaEdificioTest {

    private ServicioMapaEdificio mapa;

    /// REC-ESC 10, ESC-101 5, ESC-102 7, REC-201 40 y 301 aislado.
    @BeforeEach
    void preparar() {
        mapa = new ServicioMapaEdificio();
        mapa.agregarNodo(new NodoEdificio("REC", TipoNodo.RECEPCION, "Recepción", "A", 1, 0, 0));
        mapa.agregarNodo(new NodoEdificio("ESC", TipoNodo.ESCALERA, "Escalera", "A", 1, 1, 0));
        mapa.agregarNodo(new NodoEdificio("101", TipoNodo.APARTAMENTO, "101", "A", 1, 2, 0));
        mapa.agregarNodo(new NodoEdificio("102", TipoNodo.APARTAMENTO, "102", "A", 1, 2, 1));
        mapa.agregarNodo(new NodoEdificio("201", TipoNodo.APARTAMENTO, "201", "B", 2, 3, 0));
        mapa.agregarNodo(new NodoEdificio("301", TipoNodo.APARTAMENTO, "301", "C", 3, 9, 9));
        mapa.agregarPasillo("REC", "ESC", 10);
        mapa.agregarPasillo("ESC", "101", 5);
        mapa.agregarPasillo("ESC", "102", 7);
        mapa.agregarPasillo("REC", "201", 40);
    }

    private static ListaDoblementeEnlazada<ListaDoblementeEnlazada<String>> grupos(String... codigos) {
        return ListaDoblementeEnlazada.de(ListaDoblementeEnlazada.de(codigos));
    }

    @Test
    @DisplayName("rechaza nodos y pasillos inválidos")
    void validaciones() {
        assertThrows(ReglaNegocioExcepcion.class,
                () -> mapa.agregarNodo(new NodoEdificio("101", TipoNodo.APARTAMENTO, "x", "A", 1, 0, 0)));
        assertThrows(ReglaNegocioExcepcion.class, () -> mapa.agregarPasillo("101", "101", 3));
        assertThrows(ReglaNegocioExcepcion.class, () -> mapa.agregarPasillo("REC", "ESC", 3));
        assertThrows(ReglaNegocioExcepcion.class, () -> mapa.agregarPasillo("101", "102", 0));
        assertThrows(NoEncontradoExcepcion.class, () -> mapa.agregarPasillo("101", "999", 3));
    }

    @Test
    @DisplayName("la ruta visita siempre el apartamento más cercano")
    void rutaVecinoMasCercano() {
        var ruta = mapa.rutaPor(grupos("201", "102", "101"));
        assertArrayEquals(new String[] {"101", "102", "201"}, ruta.paradas().aArreglo(String[]::new));
        assertEquals(15 + 12 + 57, ruta.metrosTotales()); // REC→101, 101→102, 102→201
    }

    @Test
    @DisplayName("los inalcanzables quedan fuera de la ruta y se reportan")
    void rutaConInalcanzables() {
        var ruta = mapa.rutaPor(grupos("101", "301"));
        assertArrayEquals(new String[] {"101"}, ruta.paradas().aArreglo(String[]::new));
        assertArrayEquals(new String[] {"301"}, ruta.inalcanzables().aArreglo(String[]::new));
        assertEquals(15, ruta.metrosTotales());
    }

    @Test
    @DisplayName("cerrar un pasillo cambia el camino más corto")
    void cerrarPasillo() {
        mapa.agregarPasillo("101", "201", 3);
        assertEquals(18, mapa.caminoMasCorto("REC", "201").pesoTotal());
        mapa.establecerAbiertoPasillo("101", "201", false);
        assertEquals(40, mapa.caminoMasCorto("REC", "201").pesoTotal());
    }

    @Test
    @DisplayName("impactoCierre informa lo que quedaría aislado sin modificar el mapa")
    void impactoCierre() {
        var impacto = mapa.impactoCierre("REC", "ESC");
        assertArrayEquals(new String[] {"101", "102", "301"}, impacto.aArreglo(String[]::new));
        assertEquals(1, mapa.apartamentosInalcanzables().tamano()); // solo 301: el pasillo sigue abierto
    }

    @Test
    @DisplayName("agrupa los apartamentos por bloque en orden BFS y separa los aislados")
    void porBloque() {
        var grupos = mapa.apartamentosPorBloque();
        assertEquals("B", grupos.obtener(0).bloque()); // 201 se descubre antes que 101 y 102
        assertEquals("A", grupos.obtener(1).bloque());
        assertEquals("Inalcanzables", grupos.obtener(2).bloque());
    }

    @Test
    @DisplayName("sin recepción no hay ruta")
    void sinRecepcion() {
        assertThrows(ReglaNegocioExcepcion.class, () -> new ServicioMapaEdificio().idRecepcion());
    }
}
