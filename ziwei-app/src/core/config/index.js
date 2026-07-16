/**
 * Runtime configuration adapter
 * Reads environment variables and provides unified config access.
 * Inspired by yudao-mall-uniapp sheep/config/index.js
 */

/**
 * Determine base URL based on environment
 */
function getBaseUrl() {
  // Dev mode: use dev URL
  if (process.env.NODE_ENV === 'development') {
    return import.meta.env.ZIWEI_DEV_BASE_URL || import.meta.env.ZIWEI_BASE_URL
  }

  // In production, check WeChat env version
  // #ifdef MP-WEIXIN
  try {
    const accountInfo = uni.getAccountInfoSync()
    const envVersion = accountInfo?.miniProgram?.envVersion
    if (envVersion === 'trial' || envVersion === 'develop') {
      return import.meta.env.ZIWEI_BASE_URL
    }
  } catch (_) {
    /* getAccountInfoSync may not be available */
  }
  // #endif

  return import.meta.env.ZIWEI_BASE_URL
}

const baseUrl = getBaseUrl()

if (!baseUrl) {
  console.error('[ZiWei Config] ZIWEI_BASE_URL is not configured. Please check .env file.')
} else {
  console.log(`[ZiWei] 紫薇斗数 · 洞悉天命 | API: ${baseUrl}`)
}

export { baseUrl }

export const apiPath = import.meta.env.ZIWEI_API_PATH || ''
export const timeout = parseInt(import.meta.env.ZIWEI_TIMEOUT) || 30000
export const aiTimeout = parseInt(import.meta.env.ZIWEI_AI_TIMEOUT) || 120000

export default {
  baseUrl,
  apiPath,
  timeout,
  aiTimeout,
}
