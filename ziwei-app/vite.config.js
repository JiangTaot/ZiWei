// Vite configuration for ZiWei DouShu uni-app
import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [
    uni(),
  ],
  envPrefix: 'ZIWEI_',
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: '@import "./src/uni.scss";',
        silenceDeprecations: ['import', 'legacy-js-api'],
      },
    },
  },
  build: {
    target: 'es2015',
    cssTarget: 'chrome61',
  },
})
