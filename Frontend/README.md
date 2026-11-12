# Casa Quindío · Frontend

Interfaz web del **Sistema de Gestión de Aparta-hotel (SGA)**. React 19 + Vite, sin librerías de rutas ni de estado:
solo React, un cliente `fetch` y los íconos de Phosphor.

## Cómo correrlo

```bash
# 1. Backend (Spring Boot, puerto 8080) — carga un escenario de demostración al iniciar
cd Backend && ./gradlew bootRun

# 2. Frontend (Vite, puerto 5173)
cd Frontend
npm install
npm run dev          # http://localhost:5173
npm run build        # compilación de producción en dist/
npm run lint
```

Vite reenvía `/api/*` al backend (`vite.config.js`), así que no hay que configurar CORS ni URLs.

## Roles

No hay inicio de sesión real: el menú (botón de hamburguesa) trae un selector de rol que decide qué pantallas se ven.

| Rol | Pantallas |
| --- | --- |
| **Huésped** | Reservar · Mis estancias (reservas, historial, favoritos) |
| **Recepcionista** | Panel · Reservas · Reservar · Apartamentos · Calendario · Preparación · Canal externo · Huéspedes · Mapa |
| **Administrador** | Todo lo anterior + Reportes · Administración (personal, temporadas, reloj y escenarios) |

## Estructura

```
src/
├── main.jsx            Proveedores (avisos → reloj → sesión → navegación) y estilos
├── App.jsx             Isla de navegación + pantalla activa (Suspense) + avisos
├── servicios/api.js    Cliente de la API REST: una función por endpoint, errores como `ErrorApi`
├── contexto/           Estado global: Sesion (rol), Reloj (hora del servidor), Avisos (toasts)
├── navegacion/         Navegación sin rutas: registro de pantallas + contexto con una Pila para "Atrás"
├── hooks/useConsulta   Carga de datos asíncronos con recarga y datos previos mientras actualiza
├── componentes/        Piezas reutilizables (Bisel, Boton, Modal, Insignia, Revelar, DetalleReserva…)
├── paginas/            Una pantalla por archivo, cargada con `lazy`
├── utilidades/         dinero, fechas, estados (tonos y transiciones), fotos, Pila
└── estilos/            sistema.css (tokens y componentes) · paginas.css (piezas de pantallas)
```

## Decisiones de diseño

El estilo es **Editorial Luxury**: crema cálido, serif de contraste (*Fraunces*) con *Plus Jakarta Sans* para el texto,
grano de papel y fotos de interiores (Unsplash; si no cargan se muestra un degradado de respaldo).

- **Doble bisel** (`<Bisel>`): cada tarjeta es una carcasa translúcida con un núcleo interior y radios concéntricos.
- **Botones isla** (`<Boton isla={Icono}>`): el ícono vive en su propio círculo; al pasar el cursor se desplaza en diagonal.
- **Isla de navegación flotante**: píldora de vidrio separada del borde; el ítem activo se expande con su nombre.
  La hamburguesa se transforma en X y abre un menú a pantalla completa con links que suben escalonados.
- **Movimiento**: solo `transform` y `opacity`, siempre con la curva de resorte `--muelle`
  (`cubic-bezier(0.32, 0.72, 0, 1)`). Los elementos se revelan al entrar en pantalla con `IntersectionObserver`
  (nunca `scroll`). Con `prefers-reduced-motion` todo se desactiva.
- **`backdrop-filter`** solo en capas fijas (isla, menú, fondo de los diálogos); las capas `z-index` son las sistémicas
  definidas en `:root`.
- **Responsive**: el bento de 12 columnas colapsa a una columna bajo 900 px; las rotaciones y solapamientos de la
  portada se eliminan en móvil; las tablas hacen scroll horizontal en lugar de comprimirse.
- **Accesibilidad**: foco visible, `aria-*` en pestañas, menú (`inert` cuando está cerrado) y botones de solo ícono,
  diálogos nativos `<dialog>` (foco atrapado y Esc).

## Buenas prácticas de React usadas

- **División de código por pantalla** con `lazy` + `Suspense` (`navegacion/paginas.js`).
- **`useTransition`** al navegar: la pantalla actual se mantiene (atenuada) mientras se descarga la siguiente.
- **`useOptimistic`** en el corazón de favoritos (`BotonFavorito`): responde al instante y se revierte si la API falla.
- **Acciones de formulario** (`<form action>` + `useActionState`) en los formularios de apartamentos, huéspedes,
  empleados, cargos/pagos y canal externo.
- **`useDeferredValue`** en los buscadores para que escribir no bloquee la lista.
- **Estado derivado en vez de efectos**: los formularios guardan solo lo que el usuario cambió y lo demás se calcula
  (por ejemplo, fechas por defecto según el reloj del servidor).
- **Contextos pequeños y separados** con su hook (`useSesion`, `useReloj`, `useAvisos`, `useNavegacion`).
- **Botón "Atrás" con la `Pila`** propia del proyecto (`utilidades/pila.js`), no con `history`.
- **Fechas**: todas se manejan como texto ISO y se muestran con `utilidades/fechas.js`; `hoy` sale del reloj del
  servidor (que el administrador puede simular), no del navegador.

## Cómo agregar una pantalla

1. Crea `src/paginas/MiPantalla.jsx` con `export default function MiPantalla() { … }`.
2. Regístrala en `src/navegacion/paginas.js` (`REGISTRO`) y añade su `id` al menú del rol en `MENU_POR_ROL`.
3. Si necesita un endpoint nuevo, agrégalo a `src/servicios/api.js`.
