import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import { defineConfig, globalIgnores } from 'eslint/config'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{js,jsx}'],
    extends: [
      js.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      globals: globals.browser,
      parserOptions: { ecmaFeatures: { jsx: true } },
    },
  },
  {
    // Los contextos exportan el proveedor junto con su hook (`useSesion`, `useAvisos`…): es el patrón
    // habitual de React y el recargado en caliente sigue funcionando al editar el resto del código.
    files: ['src/contexto/**', 'src/navegacion/Navegacion.jsx'],
    rules: { 'react-refresh/only-export-components': 'off' },
  },
])
