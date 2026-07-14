/**
 * Platform abstraction layer for WeChat Mini Program
 * Provides capsule info, navbar height, and environment detection.
 *
 * Simplified compared to yudao-mall (WP-only, no multi-platform provider pattern).
 */
const device = uni.getWindowInfo()
const os = uni.getDeviceInfo().platform

// Platform identification
const name = 'WechatMiniProgram'
const platform = 'miniProgram'

/**
 * Get WeChat capsule button bounding rect
 * Falls back to default values if unavailable.
 */
function getCapsule() {
  // #ifdef MP-WEIXIN
  try {
    const capsule = uni.getMenuButtonBoundingClientRect()
    if (capsule && capsule.width > 0) {
      return capsule
    }
  } catch (_) { /* fall through to defaults */ }
  // #endif

  // Default capsule (iPhone-like)
  return {
    bottom: 56,
    height: 32,
    left: 278,
    right: 365,
    top: 24,
    width: 87,
  }
}

const capsule = getCapsule()

/**
 * Get navbar height (status bar + 44px standard navbar)
 */
function getNavBar() {
  return (device.statusBarHeight || 20) + 44
}

const navbar = getNavBar()

/**
 * Check network connectivity
 * @returns {Promise<boolean>}
 */
async function checkNetwork() {
  try {
    const networkStatus = await uni.getNetworkType()
    return networkStatus.networkType !== 'none'
  } catch (_) {
    return true // Assume online if check fails
  }
}

export default {
  name,
  device,
  os,
  platform,
  capsule,
  navbar,
  checkNetwork,
}
