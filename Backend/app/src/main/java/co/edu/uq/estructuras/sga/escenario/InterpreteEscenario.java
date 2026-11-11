package co.edu.uq.estructuras.sga.escenario;

import co.edu.uq.estructuras.sga.core.apartamentos.EstadoApartamento;
import co.edu.uq.estructuras.sga.core.edificio.NodoEdificio;
import co.edu.uq.estructuras.sga.core.edificio.TipoNodo;
import co.edu.uq.estructuras.sga.core.huespedes.PerfilHuesped;
import co.edu.uq.estructuras.sga.core.personal.RolEmpleado;
import co.edu.uq.estructuras.sga.core.preparacion.RegistroPreparacion;
import co.edu.uq.estructuras.sga.core.reservas.CanalReserva;
import co.edu.uq.estructuras.sga.core.reservas.Dinero;
import co.edu.uq.estructuras.sga.core.reservas.EstadoReserva;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.core.reservas.Ocupante;
import co.edu.uq.estructuras.sga.core.tarifas.PeriodoTemporada;
import co.edu.uq.estructuras.sga.core.tarifas.TarifasTemporada;
import co.edu.uq.estructuras.sga.core.tarifas.Temporada;
import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.excepciones.DominioExcepcion;
import co.edu.uq.estructuras.sga.excepciones.ReglaNegocioExcepcion;
import co.edu.uq.estructuras.sga.service.DatosApartamento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/// Interpreta el escenario de demostración escrito en JSON.
///
/// El archivo es un objeto con una lista por cada tema (`temporadas`, `apartamentos`, `edificio`,
/// `huespedes`, `empleados`, `reservas`, `preparaciones` y `solicitudesExternas`) y cada dato lleva el
/// nombre de su campo. Las fechas pueden ser absolutas (`"2026-12-24"`) o relativas a hoy (`"hoy"`,
/// `"hoy+3"`, `"hoy-10"`), así el escenario siempre tiene llegadas "hoy". Fecha y hora se escriben
/// `"hoy-1 09:30"`. Si algo está mal, el mensaje dice dónde (por ejemplo `reservas[3]`) y qué campo.
@Component
public class InterpreteEscenario {

    /// Listas que puede traer el archivo. `descripcion` es solo texto para quien lee el archivo.
    private static final String[] SECCIONES = {"descripcion", "temporadas", "apartamentos", "edificio", "huespedes",
            "empleados", "reservas", "preparaciones", "solicitudesExternas"};

    private final JsonMapper mapeador = JsonMapper.builder().build();

    /// Fecha base para las fechas relativas (`hoy`, `hoy+3`...).
    private LocalDate hoy;
    /// Momento base para las horas por defecto de las reservas.
    private LocalDateTime ahora;
    /// Dónde está el interpretador dentro del archivo (por ejemplo `reservas[3]`), para los mensajes de error.
    private String ubicacion;

    /// Convierte el texto JSON en un [Escenario]. No toca los datos del sistema: solo lee.
    ///
    /// @param contenido texto del archivo de escenario
    /// @param ahora momento actual, base de las fechas relativas
    /// @return el escenario interpretado
    /// @throws ReglaNegocioExcepcion si el JSON no es válido o algún dato no se entiende
    public synchronized Escenario interpretar(String contenido, LocalDateTime ahora) {
        this.ahora = ahora;
        this.hoy = ahora.toLocalDate();
        this.ubicacion = "archivo";
        JsonNode raiz;
        try {
            raiz = mapeador.readTree(contenido);
        } catch (JacksonException excepcion) {
            throw new ReglaNegocioExcepcion("Archivo de escenario no es un JSON válido: " + excepcion.getOriginalMessage());
        }
        if (raiz == null || !raiz.isObject()) {
            throw error("se esperaba un objeto JSON con las listas del escenario");
        }
        for (String nombre : raiz.propertyNames()) {
            if (!esSeccion(nombre)) {
                throw error("sección desconocida '" + nombre + "'");
            }
        }
        Escenario escenario = new Escenario(new ListaDoblementeEnlazada<>(), new ListaDoblementeEnlazada<>(), new ListaDoblementeEnlazada<>(),
                new ListaDoblementeEnlazada<>(), new ListaDoblementeEnlazada<>(), new ListaDoblementeEnlazada<>(), new ListaDoblementeEnlazada<>(),
                new ListaDoblementeEnlazada<>(), new ListaDoblementeEnlazada<>(), new ListaDoblementeEnlazada<>());

        recorrer(raiz, "temporadas", item -> escenario.temporadas().agregarUltimo(new PeriodoTemporada(
                enumeracion(Temporada.class, item, "temporada"), fecha(texto(item, "desde")), fecha(texto(item, "hasta")))));
        recorrer(raiz, "apartamentos", item -> apartamento(escenario, item));
        JsonNode edificio = raiz.get("edificio");
        if (edificio != null) {
            recorrer(edificio, "nodos", "edificio.nodos", item -> escenario.nodos().agregarUltimo(nodo(item)));
            recorrer(edificio, "pasillos", "edificio.pasillos", item -> escenario.pasillos().agregarUltimo(
                    new Escenario.LineaPasillo(texto(item, "desde").toUpperCase(), texto(item, "hasta").toUpperCase(),
                            decimal(item, "metros"), !item.has("abierto") || item.get("abierto").asBoolean(true))));
        }
        recorrer(raiz, "huespedes", item -> escenario.huespedes().agregarUltimo(new Escenario.LineaHuesped(
                perfil(item, "nombre", "fechaNacimiento", opcional(item, "foto")), textos(item, "favoritos"))));
        recorrer(raiz, "empleados", item -> escenario.empleados().agregarUltimo(new Escenario.LineaEmpleado(
                texto(item, "id"), texto(item, "nombre"), enumeracion(RolEmpleado.class, item, "rol"),
                opcional(item, "correo"), opcional(item, "telefono"))));
        recorrer(raiz, "reservas", item -> reserva(escenario, item));
        recorrer(raiz, "preparaciones", item -> {
            LocalDateTime inicio = fechaHora(texto(item, "inicio"));
            escenario.registrosPreparacion().agregarUltimo(new RegistroPreparacion(texto(item, "apartamento").toUpperCase(),
                    inicio, inicio.plusMinutes(entero(item, "minutos"))));
        });
        recorrer(raiz, "solicitudesExternas", item -> {
            JsonNode titular = exigir(item, "titular");
            escenario.solicitudesExternas().agregarUltimo(new Escenario.LineaSolicitudExterna(texto(item, "plataforma"),
                    texto(item, "apartamento").toUpperCase(), new Estancia(fecha(texto(item, "entrada")), fecha(texto(item, "salida"))),
                    perfil(titular, "nombre", "fechaNacimiento", null), ocupantes(item)));
        });
        return escenario;
    }

    /// Indica si `nombre` es una de las listas que acepta el archivo.
    private static boolean esSeccion(String nombre) {
        for (String seccion : SECCIONES) {
            if (seccion.equals(nombre)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------ secciones

    /// Apartamento y, si trae `estado`, la línea que lo deja en ese estado.
    private void apartamento(Escenario escenario, JsonNode item) {
        JsonNode tarifas = exigir(item, "tarifas");
        String codigo = texto(item, "codigo");
        escenario.apartamentos().agregarUltimo(new DatosApartamento(codigo, texto(item, "nombre"), texto(item, "bloque"),
                entero(item, "piso"), entero(item, "dormitorios"), entero(item, "capacidad"), lista(textos(item, "caracteristicas")),
                new TarifasTemporada(dinero(tarifas, "baja"), dinero(tarifas, "media"), dinero(tarifas, "alta"))));
        if (!opcional(item, "estado").isEmpty()) {
            escenario.estadosApartamento().agregarUltimo(new Escenario.LineaEstadoApartamento(codigo.toUpperCase(),
                    enumeracion(EstadoApartamento.class, item, "estado"), opcional(item, "motivo")));
        }
    }

    /// Nodo del mapa del edificio; el bloque es opcional (la recepción no tiene).
    private NodoEdificio nodo(JsonNode item) {
        String bloque = opcional(item, "bloque");
        return new NodoEdificio(texto(item, "id").toUpperCase(), enumeracion(TipoNodo.class, item, "tipo"), texto(item, "etiqueta"),
                bloque.isEmpty() ? "-" : bloque, entero(item, "piso"), decimal(item, "x"), decimal(item, "y"));
    }

    /// Reserva con su estado, lo pagado y sus ocupantes.
    private void reserva(Escenario escenario, JsonNode item) {
        Estancia estancia = new Estancia(fecha(texto(item, "entrada")), fecha(texto(item, "salida")));
        EstadoReserva estado = enumeracion(EstadoReserva.class, item, "estado");
        String llegada = opcional(item, "horaLlegada");
        String creada = opcional(item, "creada");
        escenario.reservas().agregarUltimo(new Escenario.LineaReserva(texto(item, "apartamento").toUpperCase(), texto(item, "titular"),
                ocupantes(item), estancia, enumeracion(CanalReserva.class, item, "canal"), estado,
                llegada.isEmpty() ? null : LocalTime.parse(llegada),
                item.has("pagado") ? dinero(item, "pagado") : Dinero.CERO,
                creada.isEmpty() ? creacionPorDefecto(estancia, estado) : fechaHora(creada)));
    }

    /// Cuándo se creó una reserva si el archivo no lo dice: hace 10 días de la entrada (o hace 2 horas si es más reciente).
    private LocalDateTime creacionPorDefecto(Estancia estancia, EstadoReserva estado) {
        LocalDateTime haceDosHoras = ahora.withSecond(0).withNano(0).minusHours(2);
        if (estado == EstadoReserva.PENDIENTE) {
            return haceDosHoras;
        }
        LocalDateTime diezDiasAntes = estancia.ingreso().minusDays(10).atTime(10, 0);
        return diezDiasAntes.isBefore(haceDosHoras) ? diezDiasAntes : haceDosHoras;
    }

    /// Datos personales de un huésped o del titular de una solicitud externa.
    private PerfilHuesped perfil(JsonNode item, String campoNombre, String campoNacimiento, String foto) {
        return new PerfilHuesped(texto(item, "documento"), texto(item, campoNombre), fecha(texto(item, campoNacimiento)),
                opcional(item, "telefono"), opcional(item, "correo"), foto == null || foto.isEmpty() ? null : foto);
    }

    /// Lista `ocupantes` del elemento: cada uno con `nombre` y `fechaNacimiento`.
    private ListaDoblementeEnlazada<Ocupante> ocupantes(JsonNode item) {
        ListaDoblementeEnlazada<Ocupante> ocupantes = new ListaDoblementeEnlazada<>();
        JsonNode lista = item.get("ocupantes");
        if (lista != null) {
            for (JsonNode ocupante : lista) {
                ocupantes.agregarUltimo(new Ocupante(texto(ocupante, "nombre"), fecha(texto(ocupante, "fechaNacimiento"))));
            }
        }
        return ocupantes;
    }

    // ------------------------------------------------------------ fechas

    /// Fecha absoluta (`2026-12-24`) o relativa a hoy (`hoy`, `hoy+3`, `hoy-10`; también `T`, `T+3`).
    LocalDate fecha(String valor) {
        String texto = valor.trim().toLowerCase();
        for (String prefijo : new String[]{"hoy", "t"}) {
            if (texto.equals(prefijo)) {
                return hoy;
            }
            if (texto.startsWith(prefijo + "+") || texto.startsWith(prefijo + "-")) {
                return hoy.plusDays(Long.parseLong(texto.substring(prefijo.length()).replace("+", "")));
            }
        }
        return LocalDate.parse(texto);
    }

    /// Fecha y hora separadas por espacio (`hoy-1 09:30`); sin hora se usan las 10:00.
    LocalDateTime fechaHora(String valor) {
        String[] partes = valor.trim().split("[ @]+");
        LocalTime reloj = partes.length > 1 ? LocalTime.parse(partes[1]) : LocalTime.of(10, 0);
        return fecha(partes[0]).atTime(reloj);
    }

    // ------------------------------------------------------------ lectura de campos

    /// Aplica `accion` a cada elemento de la lista `nombre` de `padre`, anotando dónde está para los errores.
    private void recorrer(JsonNode padre, String nombre, Consumer<JsonNode> accion) {
        recorrer(padre, nombre, nombre, accion);
    }

    private void recorrer(JsonNode padre, String nombre, String ruta, Consumer<JsonNode> accion) {
        JsonNode lista = padre.get(nombre);
        if (lista == null) {
            return;
        }
        if (!lista.isArray()) {
            ubicacion = ruta;
            throw error("se esperaba una lista");
        }
        int indice = 0;
        for (JsonNode item : lista) {
            ubicacion = ruta + "[" + indice++ + "]";
            try {
                accion.accept(item);
            } catch (DominioExcepcion | IllegalArgumentException | DateTimeParseException | ArithmeticException excepcion) {
                throw error(excepcion.getMessage());
            }
        }
    }

    /// Campo obligatorio de tipo objeto.
    private JsonNode exigir(JsonNode item, String campo) {
        JsonNode valor = item.get(campo);
        if (valor == null || valor.isNull()) {
            throw new IllegalArgumentException("falta el campo '" + campo + "'");
        }
        return valor;
    }

    /// Campo de texto obligatorio.
    private String texto(JsonNode item, String campo) {
        return exigir(item, campo).asString().trim();
    }

    /// Campo de texto opcional: vacío si no está.
    private static String opcional(JsonNode item, String campo) {
        JsonNode valor = item.get(campo);
        return valor == null || valor.isNull() ? "" : valor.asString().trim();
    }

    /// Campo entero obligatorio.
    private int entero(JsonNode item, String campo) {
        JsonNode valor = exigir(item, campo);
        if (!valor.isNumber()) {
            throw new IllegalArgumentException("el campo '" + campo + "' debe ser un número");
        }
        return valor.asInt();
    }

    /// Campo decimal obligatorio.
    private double decimal(JsonNode item, String campo) {
        JsonNode valor = exigir(item, campo);
        if (!valor.isNumber()) {
            throw new IllegalArgumentException("el campo '" + campo + "' debe ser un número");
        }
        return valor.asDouble();
    }

    /// Campo de dinero obligatorio, en pesos enteros.
    private Dinero dinero(JsonNode item, String campo) {
        JsonNode valor = exigir(item, campo);
        if (!valor.isNumber()) {
            throw new IllegalArgumentException("el campo '" + campo + "' debe ser un número");
        }
        return Dinero.de(valor.asLong());
    }

    /// Valor de una enumeración, con un mensaje que nombra el campo y los valores posibles.
    private <E extends Enum<E>> E enumeracion(Class<E> tipo, JsonNode item, String campo) {
        String valor = texto(item, campo);
        try {
            return Enum.valueOf(tipo, valor.toUpperCase());
        } catch (IllegalArgumentException excepcion) {
            StringBuilder posibles = new StringBuilder();
            for (E constante : tipo.getEnumConstants()) {
                posibles.append(posibles.isEmpty() ? "" : ", ").append(constante.name());
            }
            throw new IllegalArgumentException("el campo '" + campo + "' no puede ser '" + valor + "'; use uno de " + posibles);
        }
    }

    /// Lista de textos opcional (por ejemplo `favoritos`).
    private static String[] textos(JsonNode item, String campo) {
        JsonNode lista = item.get(campo);
        if (lista == null || !lista.isArray()) {
            return new String[0];
        }
        String[] textos = new String[lista.size()];
        int i = 0;
        for (JsonNode elemento : lista) {
            textos[i++] = elemento.asString().trim();
        }
        return textos;
    }

    /// Convierte un arreglo de textos en la lista propia.
    private static ListaDoblementeEnlazada<String> lista(String[] valores) {
        ListaDoblementeEnlazada<String> items = new ListaDoblementeEnlazada<>();
        for (String valor : valores) {
            if (!valor.isBlank()) {
                items.agregarUltimo(valor);
            }
        }
        return items;
    }

    /// Error con la ubicación dentro del archivo.
    private ReglaNegocioExcepcion error(String detalle) {
        return new ReglaNegocioExcepcion("Archivo de escenario, " + ubicacion + ": " + detalle);
    }
}
