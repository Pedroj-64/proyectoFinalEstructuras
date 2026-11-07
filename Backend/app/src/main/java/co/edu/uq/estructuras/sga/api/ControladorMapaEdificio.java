package co.edu.uq.estructuras.sga.api;



import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.AnalisisGrafoDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.CaminoDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.GrupoBloqueDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.MapaEdificioDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.NodoDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.PasilloDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.SolicitudEstadoPasillo;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.SolicitudNodo;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.SolicitudPasillo;
import co.edu.uq.estructuras.sga.core.edificio.NodoEdificio;
import co.edu.uq.estructuras.sga.core.edificio.TipoNodo;
import co.edu.uq.estructuras.sga.api.dto.MapeadorDto;
import co.edu.uq.estructuras.sga.core.apartamentos.Apartamento;
import co.edu.uq.estructuras.sga.estructuras.GrafoPonderado;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.repository.RepositorioApartamentos;
import co.edu.uq.estructuras.sga.service.ServicioMapaEdificio;

/// Endpoints del mapa del edificio: nodos, pasillos, rutas y análisis de conectividad.
@RestController
@RequestMapping("/api/mapa-edificio")
public class ControladorMapaEdificio {

    private final ServicioMapaEdificio mapaEdificio;
    private final RepositorioApartamentos apartamentos;

    /// Recibe por inyección el servicio del mapa y el repositorio de apartamentos.
    public ControladorMapaEdificio(ServicioMapaEdificio mapaEdificio, RepositorioApartamentos apartamentos) {
        this.mapaEdificio = mapaEdificio;
        this.apartamentos = apartamentos;
    }

    /// Nodos (con el estado en vivo de cada apartamento) y pasillos.
    @GetMapping
    public MapaEdificioDto mapear() {
        NodoDto[] nodos = MapeadorDto.aArreglo(mapaEdificio.nodos(), this::aDto, NodoDto[]::new);
        PasilloDto[] pasillos = MapeadorDto.aArreglo(mapaEdificio.pasillos(), MapeadorDto::aDto, PasilloDto[]::new);
        return new MapaEdificioDto(nodos, pasillos);
    }

    /// Agrega un punto al mapa.
    @PostMapping("/nodos")
    @ResponseStatus(HttpStatus.CREATED)
    public NodoDto agregarNodo(@Valid @RequestBody SolicitudNodo solicitud) {
        NodoEdificio nodo = new NodoEdificio(solicitud.id().trim().toUpperCase(), solicitud.tipo(), solicitud.etiqueta(),
                solicitud.bloque(), solicitud.piso(), solicitud.x(), solicitud.y());
        mapaEdificio.agregarNodo(nodo);
        return aDto(nodo);
    }

    /// Agrega un pasillo entre dos puntos.
    @PostMapping("/pasillos")
    @ResponseStatus(HttpStatus.CREATED)
    public PasilloDto agregarPasillo(@Valid @RequestBody SolicitudPasillo solicitud) {
        mapaEdificio.agregarPasillo(solicitud.desde(), solicitud.hasta(), solicitud.metros());
        return new PasilloDto(solicitud.desde(), solicitud.hasta(), solicitud.metros(), true);
    }

    /// Abre o cierra un pasillo.
    @PatchMapping("/pasillos")
    public MapaEdificioDto establecerEstadoPasillo(@Valid @RequestBody SolicitudEstadoPasillo solicitud) {
        mapaEdificio.establecerAbiertoPasillo(solicitud.desde(), solicitud.hasta(), solicitud.abierto());
        return mapear();
    }

    /// Camino más corto entre dos puntos (Dijkstra).
    @GetMapping("/ruta")
    public CaminoDto camino(@RequestParam String desde, @RequestParam String hasta) {
        GrafoPonderado.Camino<String> camino = mapaEdificio.caminoMasCorto(desde, hasta);
        return new CaminoDto(camino.vertices().aArreglo(String[]::new),
                camino.esAlcanzable() ? camino.pesoTotal() : -1, camino.esAlcanzable());
    }

    /// Análisis de conectividad con BFS/DFS.
    @GetMapping("/analisis")
    public AnalisisGrafoDto analisis(@RequestParam(required = false) String inicio) {
        String origen = inicio == null || inicio.isBlank() ? mapaEdificio.idRecepcion() : inicio.trim().toUpperCase();
        ListaDoblementeEnlazada<ListaDoblementeEnlazada<String>> componentes = mapaEdificio.componentesConexos();
        String[][] arreglosComponentes = MapeadorDto.aArreglo(componentes, c -> c.aArreglo(String[]::new), String[][]::new);
        return new AnalisisGrafoDto(arreglosComponentes,
                mapaEdificio.apartamentosInalcanzables().aArreglo(String[]::new),
                MapeadorDto.aArreglo(mapaEdificio.apartamentosPorBloque(),
                        grupo -> new GrupoBloqueDto(grupo.bloque(), grupo.codigosApartamentos().aArreglo(String[]::new)),
                        GrupoBloqueDto[]::new),
                mapaEdificio.anchuraPrimeroDesde(origen).aArreglo(String[]::new),
                mapaEdificio.profundidadPrimeroDesde(origen).aArreglo(String[]::new));
    }

    /// Apartamentos que quedarían inalcanzables si se cerrara el pasillo.
    @GetMapping("/impacto-cierre")
    public String[] impactoCierre(@RequestParam String desde, @RequestParam String hasta) {
        return mapaEdificio.impactoCierre(desde, hasta).aArreglo(String[]::new);
    }

    /// Convierte un nodo a DTO; si es un apartamento le agrega su estado actual para colorearlo en el mapa.
    private NodoDto aDto(NodoEdificio nodo) {
        String estado = null;
        if (nodo.tipo() == TipoNodo.APARTAMENTO) {
            Apartamento apartamento = apartamentos.buscarPorCodigo(nodo.id());
            estado = apartamento == null ? null : apartamento.obtenerEstado().name();
        }
        return MapeadorDto.aDto(nodo, estado);
    }
}
