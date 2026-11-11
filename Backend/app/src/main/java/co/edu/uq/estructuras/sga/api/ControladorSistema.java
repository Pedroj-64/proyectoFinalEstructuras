package co.edu.uq.estructuras.sga.api;

import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones;
import co.edu.uq.estructuras.sga.config.CargadorDatosDemostracion;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.service.ServicioEscenario;
import co.edu.uq.estructuras.sga.service.ServicioReloj;
import co.edu.uq.estructuras.sga.service.ServicioVencimientos;

import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.RelojDto;
import co.edu.uq.estructuras.sga.api.dto.DtosOperaciones.SolicitudReloj;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/// Carga de datos (botón de escenario de prueba / subir archivo) y reloj simulado para las demostraciones.
@RestController
@RequestMapping("/api/sistema")
public class ControladorSistema {

    private final ServicioEscenario escenarios;
    private final ServicioReloj reloj;
    private final ServicioVencimientos vencimientos;

    /// Recibe por inyección el servicio de escenarios, el reloj del sistema y el servicio de vencimientos.
    public ControladorSistema(ServicioEscenario escenarios, ServicioReloj reloj, ServicioVencimientos vencimientos) {
        this.escenarios = escenarios;
        this.reloj = reloj;
        this.vencimientos = vencimientos;
    }

    /// Carga el escenario de demostración incluido.
    @PostMapping("/escenario/por-defecto")
    public ServicioEscenario.ResumenCarga cargarPorDefecto() {
        return escenarios.cargar(CargadorDatosDemostracion.leerEscenarioPorDefecto());
    }

    /// Carga un escenario desde un archivo JSON subido.
    @PostMapping(value = "/escenario/cargar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServicioEscenario.ResumenCarga subir(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        if (archivo.isEmpty()) {
            throw new ReglaNegocioExcepcion("El archivo de escenario está vacío");
        }
        return escenarios.cargar(new String(archivo.getBytes(), StandardCharsets.UTF_8));
    }

    /// Descarga el escenario incluido para usarlo como plantilla.
    @GetMapping("/escenario/plantilla")
    public ResponseEntity<String> plantilla() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=escenario-sga.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(CargadorDatosDemostracion.leerEscenarioPorDefecto());
    }

    /// Hora actual del sistema e indicador de si está simulada.
    @GetMapping("/reloj")
    public RelojDto reloj() {
        return new RelojDto(reloj.ahora(), reloj.estaSimulado());
    }

    /// Mueve el reloj del sistema y vence de inmediato lo que ya cumplió su plazo.
    @PutMapping("/reloj")
    public RelojDto viajar(@Valid @RequestBody SolicitudReloj solicitud) {
        reloj.viajarA(solicitud.fechaHora());
        vencimientos.vencerVencidas();
        return reloj();
    }

    /// Vuelve el reloj a la hora real.
    @DeleteMapping("/reloj")
    public RelojDto reiniciarReloj() {
        reloj.reinicio();
        return reloj();
    }
}
