/**
 * API module auto-discovery and aggregation
 * Uses import.meta.glob to find all domain API modules and merge them into a single $api object.
 *
 * Usage: import zw from '@/core'; zw.$api.chart.calculate(...)
 *
 * Inspired by yudao-mall-uniapp sheep/api/index.js
 */

// Auto-discover all domain API modules (./chart/index.js, ./user/index.js, ./ai/index.js)
const files = import.meta.glob('./*/*.js', { eager: true })
let api = {}
Object.keys(files).forEach((key) => {
  // Extract domain name from path: './chart/index.js' → 'chart'
  const domain = key.replace(/(.*\/)*([^.]+)\/index\.js$/i, '$2')
  if (domain) {
    api = {
      ...api,
      [domain]: files[key].default,
    }
  }
})

export default api
