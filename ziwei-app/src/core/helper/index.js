/**
 * General utility functions
 * Inspired by yudao-mall-uniapp sheep/helper/index.js
 */

/**
 * Sleep for a given number of milliseconds
 * @param {number} ms
 * @returns {Promise<void>}
 */
export function sleep(ms = 300) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

/**
 * Generate a GUID-like string
 * @param {number} [len=16]
 * @returns {string}
 */
export function guid(len = 16) {
  const chars = 'abcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < len; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

/**
 * Deep clone an object using JSON parse/stringify
 * @template T
 * @param {T} obj
 * @returns {T}
 */
export function deepClone(obj) {
  if (obj === null || obj === undefined) return obj
  return JSON.parse(JSON.stringify(obj))
}

/**
 * Deep merge source into target (mutates target)
 * @param {Object} target
 * @param {Object} source
 * @returns {Object}
 */
export function deepMerge(target, source) {
  for (const key in source) {
    if (Object.prototype.hasOwnProperty.call(source, key)) {
      if (source[key] instanceof Object && !Array.isArray(source[key])) {
        if (!target[key]) target[key] = {}
        deepMerge(target[key], source[key])
      } else {
        target[key] = source[key]
      }
    }
  }
  return target
}

/**
 * Format a date/time value
 * @param {number|string|Date} dateTime - Timestamp (ms) or date string
 * @param {string} [fmt='yyyy-MM-dd hh:mm:ss']
 * @returns {string}
 */
export function timeFormat(dateTime, fmt = 'yyyy-MM-dd hh:mm:ss') {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  if (isNaN(date.getTime())) return ''

  const o = {
    'M+': date.getMonth() + 1,
    'd+': date.getDate(),
    'h+': date.getHours(),
    'm+': date.getMinutes(),
    's+': date.getSeconds(),
    'q+': Math.floor((date.getMonth() + 3) / 3),
    S: date.getMilliseconds(),
  }

  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length))
  }

  for (const k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(
        RegExp.$1,
        RegExp.$1.length === 1 ? o[k] : ('00' + o[k]).substr(('' + o[k]).length),
      )
    }
  }

  return fmt
}

/**
 * Format a timestamp to relative time string (e.g., "刚刚", "5分钟前")
 * @param {number|string} timestamp
 * @returns {string}
 */
export function timeFrom(timestamp) {
  if (!timestamp) return ''
  const now = Date.now()
  const diff = now - new Date(timestamp).getTime()

  if (diff < 0) return '刚刚'

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const month = 30 * day

  if (diff < minute) return '刚刚'
  if (diff < hour) return Math.floor(diff / minute) + '分钟前'
  if (diff < day) return Math.floor(diff / hour) + '小时前'
  if (diff < month) return Math.floor(diff / day) + '天前'
  return timeFormat(timestamp, 'yyyy-MM-dd')
}

/**
 * Show a toast notification
 * @param {string} title
 * @param {'success'|'error'|'none'|'loading'} [icon='none']
 * @param {number} [duration=2000]
 */
export function toast(title, icon = 'none', duration = 2000) {
  uni.showToast({ title, icon, duration })
}

/**
 * Copy text to clipboard
 * @param {string} text
 */
export function copyText(text) {
  uni.setClipboardData({
    data: text,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    },
  })
}

/**
 * Build query string from params object
 * @param {Object} data
 * @param {boolean} [isPrefix=false] - Whether to prefix with '?'
 * @returns {string}
 */
export function queryParams(data, isPrefix = false) {
  if (!data || Object.keys(data).length === 0) return ''
  const prefix = isPrefix ? '?' : ''
  const result = Object.keys(data)
    .filter((k) => data[k] != null && data[k] !== '')
    .map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(data[k])}`)
    .join('&')
  return result ? prefix + result : ''
}

/**
 * Pad a number with leading zero
 * @param {number} num
 * @param {number} [length=2]
 * @returns {string}
 */
export function padZero(num, length = 2) {
  return String(num).padStart(length, '0')
}

/**
 * Get rpx value from px
 * @param {number} px
 * @returns {number}
 */
export function getPx(px) {
  const { screenWidth } = uni.getSystemInfoSync()
  return (screenWidth * px) / 750
}

/**
 * Check if value is empty (null, undefined, empty string, empty array, empty object)
 * @param {*} value
 * @returns {boolean}
 */
export function isEmpty(value) {
  if (value == null) return true
  if (typeof value === 'string') return value.trim() === ''
  if (Array.isArray(value)) return value.length === 0
  if (typeof value === 'object') return Object.keys(value).length === 0
  return false
}
