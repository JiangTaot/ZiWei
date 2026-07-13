// Inject missing Vue exports during esbuild pre-bundling
const vuePolyfillContent = `
export const injectHook = (type, hook, target) => {
  if (!target) target = null
  if (target) {
    const hooks = target[type] || (target[type] = [])
    const wrappedHook = (...args) => { if (!target.isUnmounted) return hook(...args) }
    hooks.push(wrappedHook)
    return wrappedHook
  }
}
export const isInSSRComponentSetup = false
`

module.exports = {
  name: 'vue-polyfill-esbuild',
  setup(build) {
    // Intercept @vue/shared to add getEscapedCssVarName
    build.onResolve({ filter: /^@vue\/shared$/ }, (args) => {
      return { path: args.path, namespace: 'vue-shared-polyfill' }
    })
    build.onLoad({ filter: /.*/, namespace: 'vue-shared-polyfill' }, async (args) => {
      const fs = require('fs')
      const sharedPath = require('path').resolve(__dirname, 'node_modules/@vue/shared/dist/shared.esm-bundler.js')
      let content = fs.readFileSync(sharedPath, 'utf-8')
      content += '\n' + 'export function getEscapedCssVarName(key, doubleEscape) { if (doubleEscape) { key = key.replace(/\\\\/g, "\\\\\\\\") } return key.replace(/([A-Z])/g, "-$1").replace(/^-/, "").toLowerCase() }'
      return { contents: content, loader: 'js' }
    })

    // Intercept vue to add injectHook + isInSSRComponentSetup
    build.onResolve({ filter: /^vue$/ }, (args) => {
      return { path: args.path, namespace: 'vue-polyfill' }
    })
    build.onLoad({ filter: /.*/, namespace: 'vue-polyfill' }, async (args) => {
      const fs = require('fs')
      const vuePath = require('path').resolve(__dirname, 'node_modules/vue/dist/vue.runtime.esm-bundler.js')
      let content = fs.readFileSync(vuePath, 'utf-8')
      content += '\n' + vuePolyfillContent
      return { contents: content, loader: 'js' }
    })
  },
}
