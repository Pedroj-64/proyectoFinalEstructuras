package co.edu.uq.estructuras.sga.core.apartamentos;

import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.tarifas.TarifasTemporada;
import co.edu.uq.estructuras.sga.core.tarifas.Temporada;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.excepciones.TransicionEstadoInvalidaExcepcion;

import java.time.LocalDate;

/// Apartamento que se alquila completo a un solo grupo. Orden natural: por código.
public class Apartamento implements Comparable<Apartamento> {

    private final String codigo;
    private String nombre;
    private String bloque;
    private int piso;
    private int habitaciones;
    private int capacidad;
    private ListaDoblementeEnlazada<String> caracteristicas;
    private TarifasTemporada tarifas;
    private EstadoApartamento estado = EstadoApartamento.PREPARADO;
    private String motivoFueraDeServicio;
    private LocalDate fueraDeServicioDesde;

    /// Crea el apartamento en estado Preparado; exige código y valida capacidad, habitaciones y tarifas.
    public Apartamento(String codigo, String nombre, String bloque, int piso, int habitaciones, int capacidad,
                     ListaDoblementeEnlazada<String> caracteristicas, TarifasTemporada tarifas) {
        if (codigo == null || codigo.isBlank()) {
            throw new ReglaNegocioExcepcion("El código del apartamento es obligatorio");
        }
        this.codigo = codigo.trim().toUpperCase();
        actualizarDetalles(nombre, bloque, piso, habitaciones, capacidad, caracteristicas);
        actualizarTarifas(tarifas);
    }

    /// Actualiza los datos descriptivos validando capacidad y habitaciones.
    public void actualizarDetalles(String nombre, String bloque, int piso, int habitaciones, int capacidad,
                              ListaDoblementeEnlazada<String> caracteristicas) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioExcepcion("El nombre del apartamento es obligatorio");
        }
        if (habitaciones < 1 || capacidad < 1) {
            throw new ReglaNegocioExcepcion("Dormitorios y capacidad deben ser mayores que cero");
        }
        this.nombre = nombre.trim();
        this.bloque = bloque == null ? "" : bloque.trim();
        this.piso = piso;
        this.habitaciones = habitaciones;
        this.capacidad = capacidad;
        this.caracteristicas = caracteristicas == null ? new ListaDoblementeEnlazada<>() : caracteristicas;
    }

    /// Reemplaza las tarifas por persona.
    public void actualizarTarifas(TarifasTemporada nuevasTarifas) {
        if (nuevasTarifas == null) {
            throw new ReglaNegocioExcepcion("Las tarifas son obligatorias");
        }
        this.tarifas = nuevasTarifas;
    }

    /// Pasa a otro estado operativo validando el ciclo de vida.
    public void cambiarEstado(EstadoApartamento destino) {
        if (destino == EstadoApartamento.FUERA_DE_SERVICIO) {
            throw new ReglaNegocioExcepcion("Use marcarFueraDeServicio: el motivo es obligatorio");
        }
        exigirTransicion(destino);
        estado = destino;
        motivoFueraDeServicio = null;
        fueraDeServicioDesde = null;
    }

    /// Saca el apartamento de servicio guardando el motivo y la fecha.
    public void marcarFueraDeServicio(String motivo, LocalDate desde) {
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaNegocioExcepcion("Debe registrar el motivo para dejar el apartamento fuera de servicio");
        }
        exigirTransicion(EstadoApartamento.FUERA_DE_SERVICIO);
        estado = EstadoApartamento.FUERA_DE_SERVICIO;
        motivoFueraDeServicio = motivo.trim();
        fueraDeServicioDesde = desde;
    }

    /// Lanza [TransicionEstadoInvalidaExcepcion] si el estado actual no permite pasar a `destino`.
    private void exigirTransicion(EstadoApartamento destino) {
        if (!estado.puedePasarA(destino)) {
            throw new TransicionEstadoInvalidaExcepcion("El apartamento " + codigo,
                    estado.nombreMostrado(), destino.nombreMostrado());
        }
    }

    /// Solo se usa al cargar un escenario, donde el estado ya viene decidido.
    public void restaurarEstado(EstadoApartamento restaurado, String motivo, LocalDate desde) {
        estado = restaurado;
        motivoFueraDeServicio = restaurado == EstadoApartamento.FUERA_DE_SERVICIO ? motivo : null;
        fueraDeServicioDesde = restaurado == EstadoApartamento.FUERA_DE_SERVICIO ? desde : null;
    }

    /// Tarifa por persona por noche de la temporada indicada.
    public Dinero tarifaPara(Temporada temporada) {
        return tarifas.tarifaPara(temporada);
    }

    /// Indica si se puede reservar (cualquier estado menos Fuera de servicio).
    public boolean esReservable() {
        return estado != EstadoApartamento.FUERA_DE_SERVICIO;
    }

    /// Código único (llave del árbol de apartamentos).
    public String obtenerCodigo() {
        return codigo;
    }

    /// Nombre comercial.
    public String obtenerNombre() {
        return nombre;
    }

    /// Bloque del edificio.
    public String obtenerBloque() {
        return bloque;
    }

    /// Piso.
    public int obtenerPiso() {
        return piso;
    }

    /// Número de habitaciones.
    public int obtenerHabitaciones() {
        return habitaciones;
    }

    /// Capacidad máxima de personas.
    public int obtenerCapacidad() {
        return capacidad;
    }

    /// Características (wifi, balcón...).
    public ListaDoblementeEnlazada<String> obtenerCaracteristicas() {
        return ListaDoblementeEnlazada.copiarDe(caracteristicas);
    }

    /// Tarifas por persona y noche según la temporada.
    public TarifasTemporada obtenerTarifas() {
        return tarifas;
    }

    /// Estado operativo actual.
    public EstadoApartamento obtenerEstado() {
        return estado;
    }

    /// Motivo de fuera de servicio, o `null` si está en servicio.
    public String obtenerMotivoFueraDeServicio() {
        return motivoFueraDeServicio;
    }

    /// Fecha desde la que está fuera de servicio, o `null`.
    public LocalDate obtenerFueraDeServicioDesde() {
        return fueraDeServicioDesde;
    }

    /// Orden natural: por código.
    @Override
    public int compareTo(Apartamento otro) {
        return codigo.compareTo(otro.codigo);
    }

    /// Dos apartamentos son iguales si tienen el mismo código.
    @Override
    public boolean equals(Object otro) {
        return otro instanceof Apartamento apartamento && codigo.equals(apartamento.codigo);
    }

    /// Consistente con [#equals(Object)]: usa el código.
    @Override
    public int hashCode() {
        return codigo.hashCode();
    }

    /// Representación corta para registros: código y nombre.
    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
