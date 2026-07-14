/**
 * Pinia store auto-discovery and setup
 * Uses import.meta.glob to find all store modules and provides a $store() accessor function.
 *
 * Usage:
 *   import { setupPinia } from '@/core/store'
 *   setupPinia(app)  // in main.js
 *
 *   import $store from '@/core/store'
 *   const userStore = $store('user')
 *   userStore.login(...)
 *
 * Inspired by yudao-mall-uniapp sheep/store/index.js
 */
import { createPinia } from 'pinia'
import piniaPersist from 'pinia-plugin-persist-uni'

// Auto-discover all store modules in this directory
const files = import.meta.glob('./*.js', { eager: true })
const modules = {}
Object.keys(files).forEach((key) => {
  // Extract module name: './user.js' → 'user', skip 'index.js'
  const name = key.replace(/(.*\/)*([^.]+).*/gi, '$2')
  if (name !== 'index' && files[key].default) {
    modules[name] = files[key].default
  }
})

/**
 * Setup Pinia with persistence plugin
 * Called from main.js
 * @param {Object} app - Vue app instance
 */
export const setupPinia = (app) => {
  const pinia = createPinia()
  pinia.use(piniaPersist)
  app.use(pinia)
}

/**
 * Access a store by name
 * @param {string} name - Store name (e.g., 'user', 'chart')
 * @returns {Object} Pinia store instance
 *
 * Example: const userStore = $store('user')
 */
export default (name) => {
  if (!modules[name]) {
    console.error(`[ZiWei Store] Store "${name}" not found. Available: ${Object.keys(modules).join(', ')}`)
    return null
  }
  return modules[name]()
}
