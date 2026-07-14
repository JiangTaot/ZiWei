/**
 * Core framework entry point — unified singleton for all subsystems
 *
 * Usage:
 *   import zw from '@/core'
 *   zw.$api.chart.calculate(...)
 *   zw.$store('user').isLoggedIn
 *   zw.$router.go('/pages/result/index', { chartId: '123' })
 *   zw.$helper.toast('操作成功', 'success')
 *
 * Inspired by yudao-mall-uniapp sheep/index.js
 */
import $api from './api'
import $store from './store'
import $router from './router'
import $platform from './platform'
import * as $helper from './helper'

/**
 * Framework singleton — access all subsystems through one import
 */
const zw = {
  $api,
  $store,
  $router,
  $platform,
  $helper,
}

/**
 * Initialize the framework
 * Called from App.vue onLaunch
 */
export async function ZwInit() {
  // Platform-specific initialization
  $platform.checkNetwork()

  console.log('[ZiWei] Framework initialized')
}

export default zw
