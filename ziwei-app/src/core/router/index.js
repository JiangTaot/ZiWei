/**
 * Router / Navigation helpers
 * Wraps uni navigation APIs with throttling and tabbar detection.
 *
 * Inspired by yudao-mall-uniapp sheep/router/index.js
 */
import throttle from '../helper/throttle'

// Tab bar pages (use switchTab instead of navigateTo)
const TAB_PAGES = [
  '/pages/index/index',
  '/pages/chart/index',
  '/pages/mine/index',
]

/**
 * Navigate to a page (throttled to prevent double-tap)
 * Automatically uses switchTab for tabbar pages, navigateTo for others.
 *
 * @param {string} url - Page path with optional query string
 * @param {Object} [params={}] - Query params to append
 * @param {Object} [options]
 * @param {boolean} [options.redirect=false] - Use redirectTo instead of navigateTo
 */
function go(url, params = {}, options = {}) {
  const { redirect = false } = options

  // Build query string from params
  const queryParts = []
  Object.keys(params).forEach((key) => {
    if (params[key] != null && params[key] !== '') {
      queryParts.push(`${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    }
  })

  let finalUrl = url
  if (queryParts.length > 0) {
    const separator = url.includes('?') ? '&' : '?'
    finalUrl = url + separator + queryParts.join('&')
  }

  // Throttled navigation
  throttle(() => {
    // Tabbar pages use switchTab
    if (TAB_PAGES.includes(url.split('?')[0])) {
      uni.switchTab({ url: url.split('?')[0] })
      return
    }

    if (redirect) {
      uni.redirectTo({ url: finalUrl })
    } else {
      uni.navigateTo({ url: finalUrl })
    }
  })()
}

/**
 * Navigate back
 */
function back() {
  // #ifdef H5
  window.history.back()
  // #endif
  // #ifndef H5
  uni.navigateBack()
  // #endif
}

/**
 * Redirect to a page (replaces current page)
 * @param {string} url
 * @param {Object} [params]
 */
function redirect(url, params = {}) {
  go(url, params, { redirect: true })
}

/**
 * Switch to a tab bar page
 * @param {string} url - Tab page path
 */
function switchTab(url) {
  uni.switchTab({ url })
}

/**
 * Get the current page instance
 * @returns {Object}
 */
function getCurrentPage() {
  const pages = getCurrentPages()
  return pages[pages.length - 1]
}

/**
 * Get the current route info
 * @returns {{ route: string, options: Object }}
 */
function getCurrentRoute() {
  const page = getCurrentPage()
  return {
    route: page.route,
    options: page.options || {},
  }
}

export default {
  go,
  back,
  redirect,
  switchTab,
  getCurrentPage,
  getCurrentRoute,
}
