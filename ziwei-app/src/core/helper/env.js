/**
 * WeChat Mini Program environment detection
 * Returns the current WP env version: 'develop' | 'trial' | 'release'
 * Cached since it never changes during app lifecycle.
 */
let cachedEnvVersion = null

export function getWxEnvVersion() {
  if (cachedEnvVersion !== null) {
    return cachedEnvVersion
  }
  try {
    // #ifdef MP-WEIXIN
    const accountInfo = uni.getAccountInfoSync()
    cachedEnvVersion = accountInfo?.miniProgram?.envVersion || 'release'
    // #endif
  } catch (_) {
    cachedEnvVersion = 'release'
  }
  // #ifndef MP-WEIXIN
  cachedEnvVersion = 'release'
  // #endif
  return cachedEnvVersion
}
