import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    // Fail loudly rather than drifting to 5174. The gateway's CORS policy is
    // origin-specific, so a silent port fallback makes every API call fail with
    // an opaque network error that looks like "the backend is down" — which is
    // exactly what it looked like. Better to be told the port is taken.
    strictPort: true,
  },
})
