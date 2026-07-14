/**
 * ZiWei DouShu uni-app entry point
 * Sets up Vue SSR app with Pinia store (auto-discovered via core/store).
 */
import { createSSRApp } from 'vue'
import { setupPinia } from '@/core/store'
import App from './App.vue'

export function createApp() {
  const app = createSSRApp(App)

  // Install Pinia with persistence plugin (auto-discovers all stores in core/store/)
  setupPinia(app)

  return { app }
}
