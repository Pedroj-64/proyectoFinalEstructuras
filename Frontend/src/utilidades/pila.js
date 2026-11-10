/**
 * Pila LIFO genérica construida con nodos enlazados (la misma idea que `PilaEnlazada` en Java).
 * El frontend la usa para el botón "Atrás" entre pantallas.
 */
export class Pila {
  /** Cima de la pila: un nodo `{ valor, debajo }` o `null` si está vacía. */
  #cima = null;
  /** Cantidad de elementos apilados. */
  #cantidad = 0;

  /** Apila un valor en la cima. */
  apilar(valor) {
    this.#cima = { valor, debajo: this.#cima };
    this.#cantidad++;
  }

  /** Saca y devuelve la cima; lanza un error si la pila está vacía. */
  desapilar() {
    if (!this.#cima) {
      throw new Error('La pila está vacía');
    }
    const { valor } = this.#cima;
    this.#cima = this.#cima.debajo;
    this.#cantidad--;
    return valor;
  }

  /** Devuelve la cima sin sacarla, o `undefined` si la pila está vacía. */
  ver() {
    return this.#cima?.valor;
  }

  /** Cantidad de elementos de la pila. */
  get tamano() {
    return this.#cantidad;
  }

  /** Indica si la pila está vacía. */
  estaVacia() {
    return this.#cantidad === 0;
  }

  /** Vacía la pila. */
  vaciar() {
    this.#cima = null;
    this.#cantidad = 0;
  }

  /** Recorre los elementos desde la cima hacia el fondo. */
  *[Symbol.iterator]() {
    for (let nodo = this.#cima; nodo; nodo = nodo.debajo) {
      yield nodo.valor;
    }
  }
}
