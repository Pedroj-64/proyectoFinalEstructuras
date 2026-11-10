package co.edu.uq.estructuras.sga.repository;

import co.edu.uq.estructuras.sga.estructuras.ListaDoblementeEnlazada;
import co.edu.uq.estructuras.sga.core.reservas.Estancia;
import co.edu.uq.estructuras.sga.core.reservas.Reserva;

/// Puerto para guardar y consultar reservas (patrón Repository). Es una interfaz: el dominio depende de
/// esta abstracción y no de cómo se guardan los datos (principio de inversión de dependencias).
///
/// La implementación en memoria se hará aparte. En el proyecto original guardaba las reservas de cada
/// apartamento en un árbol binario de búsqueda ordenado por fecha de entrada.
public interface RepositorioReservas {

    /// Genera el siguiente id de reserva.
    ///
    /// @return un id que todavía no se ha usado
    String siguienteId();

    /// Guarda una reserva nueva, de modo que luego se pueda buscar por id y por apartamento.
    ///
    /// @param reserva reserva que se guarda
    void guardar(Reserva reserva);

    /// Busca una reserva por su id.
    ///
    /// @param id id de la reserva
    /// @return la reserva con ese id, o `null` si no existe
    Reserva buscarPorId(String id);

    /// Busca una reserva activa del apartamento que comparta al menos una noche con `estancia`.
    ///
    /// @param codigoApartamento código del apartamento
    /// @param estancia fechas con las que se compara
    /// @return una reserva activa que se cruza con `estancia`, o `null` si no hay ninguna
    Reserva buscarSolapada(String codigoApartamento, Estancia estancia);

    /// Busca todas las reservas del apartamento, en cualquier estado, que comparten al menos una noche con `estancia`.
    ///
    /// @param codigoApartamento código del apartamento
    /// @param estancia fechas con las que se compara
    /// @return las reservas que se cruzan con `estancia` (vacía si no hay ninguna)
    ListaDoblementeEnlazada<Reserva> buscarQueSeCruzan(String codigoApartamento, Estancia estancia);

    /// Reservas de un apartamento.
    ///
    /// @param codigoApartamento código del apartamento
    /// @return las reservas del apartamento, ordenadas por fecha de entrada
    ListaDoblementeEnlazada<Reserva> buscarPorApartamento(String codigoApartamento);

    /// Todas las reservas del sistema.
    ///
    /// @return todas las reservas guardadas
    ListaDoblementeEnlazada<Reserva> buscarTodos();

    /// Cuenta las reservas de un apartamento, en cualquier estado.
    ///
    /// @param codigoApartamento código del apartamento
    /// @return cantidad de reservas del apartamento
    int contarPorApartamento(String codigoApartamento);

    /// Borra todas las reservas, por ejemplo al recargar el escenario.
    void vaciar();
}
