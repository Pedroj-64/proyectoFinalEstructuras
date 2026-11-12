import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // El front llama a `/api/...`; en desarrollo Vite lo reenvía al backend (Spring Boot, puerto 8080).
    proxy: { '/api': 'http://localhost:8080' },
  },
})
