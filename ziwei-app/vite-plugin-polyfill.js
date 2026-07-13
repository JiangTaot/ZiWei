// Polyfill missing Vue exports for @dcloudio/uni-app alpha H5 compatibility
const path = require('path')
const fs = require('fs')

// injectHook is from @vue/runtime-core, used by @dcloudio/uni-app but not exported by vue < 3.5
const VUE_EXTRA = `
import { getCurrentInstance } from 'vue'
function injectHook(type, hook, target) {
  if (!target) target = getCurrentInstance()
  if (target) {
    const hooks = target[type] || (target[type] = [])
    const wrappedHook = function() {
      if (target.isUnmounted) return
      return hook.apply(this, arguments)
    }
    hooks.push(wrappedHook)
    return wrappedHook
  }
}
export { injectHook }

// isInSSRComponentSetup — always false in client-side H5
export const isInSSRComponentSetup = false
`

// getEscapedCssVarName added in @vue/shared 3.5
const SHARED_EXTRA = `
export function getEscapedCssVarName(key, doubleEscape) {
  if (doubleEscape) { key = key.replace(/\\\\/g, '\\\\\\\\') }
  return key.replace(/([A-Z])/g, '-$1').replace(/^-/, '').toLowerCase()
}
`

module.exports = function vuePolyfill() {
  const root = __dirname
  const vueFile = path.resolve(root, 'node_modules/vue/dist/vue.runtime.esm-bundler.js')
  const sharedFile = path.resolve(root, 'node_modules/@vue/shared/dist/shared.esm-bundler.js')

  return {
    name: 'vue-uni-polyfill',
    enforce: 'pre',

    resolveId(id) {
      if (id === 'vue' || id.endsWith('/vue.runtime.esm-bundler.js')) {
        return '\0vue-polyfilled'
      }
      if (id === '@vue/shared' || id.endsWith('/shared.esm-bundler.js')) {
        return '\0vue-shared-polyfilled'
      }
      return null
    },

    load(id) {
      if (id === '\0vue-polyfilled' && fs.existsSync(vueFile)) {
        return fs.readFileSync(vueFile, 'utf-8') + VUE_EXTRA
      }
      if (id === '\0vue-shared-polyfilled' && fs.existsSync(sharedFile)) {
        return fs.readFileSync(sharedFile, 'utf-8') + SHARED_EXTRA
      }
      return null
    },
  }
}
