package co.edu.uq.estructuras.sga.api;

import co.edu.uq.estructuras.sga.api.dto.DtosApartamento;
import co.edu.uq.estructuras.sga.api.dto.DtosHuesped;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva;
import co.edu.uq.estructuras.sga.api.dto.MapeadorDto;
import co.edu.uq.estructuras.sga.repository.RepositorioReservas;
import co.edu.uq.estructuras.sga.service.ServicioHuespedes;

import co.edu.uq.estructuras.sga.api.dto.DtosApartamento.ApartamentoDto;
import co.edu.uq.estructuras.sga.api.dto.DtosHuesped.HuespedDto;
import co.edu.uq.estructuras.sga.api.dto.DtosHuesped.RegistroEstanciaDto;
import co.edu.uq.estructuras.sga.api.dto.DtosHuesped.ResultadoFavorito;
import co.edu.uq.estructuras.sga.api.dto.DtosHuesped.SolicitudFavorito;
import co.edu.uq.estructuras.sga.api.dto.DtosHuesped.SolicitudHuesped;
import co.edu.uq.estructuras.sga.api.dto.DtosReserva.ReservaDto;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/// Endpoints de huéspedes: perfil, favoritos, historial y reservas.
@RestController
@RequestMapping("/api/huespedes")
public class ControladorHuespedes {

    private final ServicioHuespedes huespedes;
    private final RepositorioReservas repositorioReservas;

    /// Recibe por inyección el servicio de huéspedes y el repositorio de reservas.
    public ControladorHuespedes(ServicioHuespedes huespedes, RepositorioReservas repositorioReservas) {
        this.huespedes = huespedes;
        this.repositorioReservas = repositorioReservas;
    }

    /// Lista todos los huéspedes.
    @GetMapping
    public HuespedDto[] buscarTodos() {
        return MapeadorDto.aArreglo(huespedes.buscarTodos(), MapeadorDto::aDto, HuespedDto[]::new);
    }

    /// Búsqueda por documento en el árbol binario de búsqueda.
    @GetMapping("/{documento}")
    public HuespedDto buscar(@PathVariable String documento) {
        return MapeadorDto.aDto(huespedes.buscar(documento));
    }

    /// Registra un huésped.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HuespedDto registrar(@Valid @RequestBody SolicitudHuesped solicitud) {
        return MapeadorDto.aDto(huespedes.registrar(MapeadorDto.aPerfil(solicitud)));
    }

    /// Actualiza el perfil de un huésped.
    @PutMapping("/{documento}")
    public HuespedDto actualizar(@PathVariable String documento, @Valid @RequestBody SolicitudHuesped solicitud) {
        return MapeadorDto.aDto(huespedes.actualizar(documento, MapeadorDto.aPerfil(solicitud)));
    }

    /// Elimina un huésped.
    @DeleteMapping("/{documento}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String documento) {
        huespedes.eliminar(documento);
    }

    /// Apartamentos favoritos del huésped.
    @GetMapping("/{documento}/favoritos")
    public ApartamentoDto[] favoritos(@PathVariable String documento) {
        return MapeadorDto.aArreglo(huespedes.favoritos(documento),
                apartamento -> MapeadorDto.aDto(apartamento, a -> repositorioReservas.contarPorApartamento(a.obtenerCodigo())),
                ApartamentoDto[]::new);
    }

    /// Agrega un favorito; si ya estaba, lo informa sin repetirlo.
    @PostMapping("/{documento}/favoritos")
    public ResultadoFavorito agregarFavorito(@PathVariable String documento, @Valid @RequestBody SolicitudFavorito solicitud) {
        boolean agregado = huespedes.agregarFavorito(documento, solicitud.codigoApartamento());
        return new ResultadoFavorito(agregado, agregado ? "Agregado a favoritos" : "Ya estaba en favoritos");
    }

    /// Quita un favorito.
    @DeleteMapping("/{documento}/favoritos/{codigoApartamento}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitarFavorito(@PathVariable String documento, @PathVariable String codigoApartamento) {
        huespedes.quitarFavorito(documento, codigoApartamento);
    }

    /// Historial de estancias finalizadas.
    @GetMapping("/{documento}/historial")
    public RegistroEstanciaDto[] historial(@PathVariable String documento) {
        return MapeadorDto.aArreglo(huespedes.historial(documento), MapeadorDto::aDto, RegistroEstanciaDto[]::new);
    }

    /// Reservas del huésped como titular.
    @GetMapping("/{documento}/reservas")
    public ReservaDto[] reservas(@PathVariable String documento) {
        return MapeadorDto.aArreglo(huespedes.reservasDe(huespedes.buscar(documento).obtenerDocumento()), MapeadorDto::aDto,
                ReservaDto[]::new);
    }
}
