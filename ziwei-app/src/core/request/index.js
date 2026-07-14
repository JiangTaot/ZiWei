/**
 * HTTP request wrapper for uni-app
 * Migrated from src/api/request.js into the core framework layer.
 *
 * Enhancements over the original:
 * - Reads BASE_URL from core/config (no more hardcoded IP)
 * - Cleaner auth/skipAuth semantics (auth: false = skip auth)
 * - Response interceptor always unwraps code !== 0 errors
 *
 * Inspired by yudao-mall-uniapp sheep/request/index.js
 */
import { baseUrl, apiPath, timeout, aiTimeout } from '../config'

const BASE_URL = baseUrl + apiPath
const TIMEOUT = timeout
const AI_TIMEOUT = aiTimeout

// Loading counter — avoids flicker on concurrent requests
const LoadingCounter = { count: 0 }

function showLoading(title = '加载中...') {
  LoadingCounter.count++
  if (LoadingCounter.count === 1) {
    uni.showLoading({ title, mask: true })
  }
}

function hideLoading() {
  if (LoadingCounter.count > 0) LoadingCounter.count--
  if (LoadingCounter.count === 0) uni.hideLoading()
}

// Error message mapping
const ERROR_MESSAGES = {
  400: '请求错误',
  401: '登录已过期，请重新登录',
  403: '拒绝访问',
  404: '请求的资源不存在',
  408: '请求超时',
  429: '请求过于频繁，请稍后再试',
  500: '服务器开小差了，请稍后再试',
  502: '网络错误',
  503: '服务暂不可用',
  504: '网络超时',
}

/**
 * 获取 auth token（从 storage 读取，userStore.login 时同步写入）
 */
function getToken() {
  try {
    return uni.getStorageSync('auth_token') || ''
  } catch (_) {
    return ''
  }
}

/**
 * Handle 401 — clear auth and notify
 */
function handleUnauthorized() {
  uni.removeStorageSync('auth_token')
  uni.removeStorageSync('user_openid')
  uni.removeStorageSync('user_id')
  // Clear user-store persist data
  try {
    uni.removeStorageSync('user-store')
  } catch (_) { /* ignore */ }
}

/**
 * Core request method
 * @param {Object} options
 * @param {string} options.url - Request URL (relative to BASE_URL)
 * @param {string} options.method - HTTP method
 * @param {Object} options.data - Request body
 * @param {Object} options.params - Query params
 * @param {Object} options.header - Additional headers
 * @param {Object} options.custom - Custom config
 * @param {boolean} options.custom.showLoading - Show loading indicator (default false)
 * @param {boolean} options.custom.showError - Show error toast (default true)
 * @param {boolean} options.custom.auth - Require auth token (default true)
 * @param {string} options.custom.loadingMsg - Custom loading message
 */
function request(options = {}) {
  const {
    url = '',
    method = 'GET',
    data = {},
    params = {},
    header = {},
    custom = {},
  } = options

  const {
    showLoading: shouldShowLoading = false,
    showError = true,
    auth = true,
    loadingMsg = '加载中...',
  } = custom

  // Show loading
  if (shouldShowLoading) {
    showLoading(loadingMsg)
  }

  // Build headers
  const headers = {
    'Content-Type': 'application/json',
    ...header,
  }

  // Inject auth token (when auth is true)
  if (auth) {
    const token = getToken()
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
  }

  // Build query string
  const queryString = Object.keys(params)
    .filter((k) => params[k] != null && params[k] !== '')
    .map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`)
    .join('&')

  let finalUrl = url.startsWith('http') ? url : `${BASE_URL}${url}`
  if (queryString) {
    finalUrl += (finalUrl.includes('?') ? '&' : '?') + queryString
  }

  // Determine timeout: AI endpoints need longer
  const reqTimeout = url.includes('/ai/') ? AI_TIMEOUT : TIMEOUT

  return new Promise((resolve, reject) => {
    uni.request({
      url: finalUrl,
      method,
      data,
      header: headers,
      timeout: reqTimeout,
      success: (res) => {
        if (shouldShowLoading) hideLoading()

        const { statusCode, data: responseData } = res

        // Handle 401
        if (statusCode === 401) {
          handleUnauthorized()
          if (showError) {
            uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
          }
          reject(new Error('Unauthorized'))
          return
        }

        // Handle HTTP errors
        if (statusCode >= 400) {
          const errMsg = ERROR_MESSAGES[statusCode] || `请求失败 (${statusCode})`
          if (showError) {
            uni.showToast({ title: errMsg, icon: 'none' })
          }
          reject(new Error(errMsg))
          return
        }

        // Handle business errors (unified response unwrapping)
        if (responseData && responseData.code !== undefined && responseData.code !== 0 && responseData.code !== 200) {
          const msg = responseData.message || responseData.msg || '操作失败'
          if (showError) {
            uni.showToast({ title: msg, icon: 'none' })
          }
          reject(new Error(msg))
          return
        }

        resolve(responseData)
      },
      fail: (err) => {
        if (shouldShowLoading) hideLoading()

        let errMsg = '网络连接失败'
        if (err.errMsg) {
          if (err.errMsg.includes('timeout')) errMsg = '请求超时，请稍后重试'
          else if (err.errMsg.includes('fail')) errMsg = '网络不可用，请检查网络连接'
        }

        if (showError) {
          uni.showToast({ title: errMsg, icon: 'none', duration: 2000 })
        }
        reject(err)
      },
    })
  })
}

// Convenience methods
const http = {
  get(url, params = {}, custom = {}) {
    return request({ url, method: 'GET', params, custom })
  },

  post(url, data = {}, custom = {}) {
    return request({ url, method: 'POST', data, custom })
  },

  put(url, data = {}, custom = {}) {
    return request({ url, method: 'PUT', data, custom })
  },

  delete(url, data = {}, custom = {}) {
    return request({ url, method: 'DELETE', data, custom })
  },
}

export { request }
export default http
