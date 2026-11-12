import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './estilos/sistema.css';
import './estilos/paginas.css';
import App from './App.jsx';
import { ProveedorAvisos } from './contexto/Avisos.jsx';
import { ProveedorReloj } from './contexto/Reloj.jsx';
import { ProveedorSesion } from './contexto/Sesion.jsx';
import { ProveedorNavegacion } from './navegacion/Navegacion.jsx';

// Orden de los proveedores: los avisos van primero porque la API los usa para mostrar errores;
// la sesión (rol) debe envolver a la navegación porque cada rol ve pantallas distintas.
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ProveedorAvisos>
      <ProveedorReloj>
        <ProveedorSesion>
          <ProveedorNavegacion>
            <App />
          </ProveedorNavegacion>
        </ProveedorSesion>
      </ProveedorReloj>
    </ProveedorAvisos>
  </StrictMode>,
);
