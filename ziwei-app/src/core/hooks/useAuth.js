/**
 * Auth composable — encapsulates WeChat login flow
 * Extracted from pages/index/index.vue
 */
import { ref } from 'vue'
import $store from '@/core/store'

/**
 * @returns {Object} Auth methods and state
 */
export function useAuth() {
  const loading = ref(false)
  const error = ref('')

  /**
   * Perform WeChat login
   * 1. Get code via uni.login
   * 2. Exchange code for token via backend
   * @returns {Promise<Object|null>} Login result or null on failure
   */
  async function wechatLogin() {
    loading.value = true
    error.value = ''

    try {
      // Step 1: Get WeChat login code
      const loginRes = await uniLogin()
      const code = loginRes.code
      if (!code) {
        error.value = '获取微信授权码失败'
        uni.showToast({ title: '获取授权码失败', icon: 'none' })
        return null
      }

      // Step 2: Exchange code for token
      const userStore = $store('user')
      if (!userStore) {
        error.value = 'Store 未初始化'
        return null
      }

      const result = await userStore.login(code)
      if (result) {
        uni.showToast({ title: '登录成功', icon: 'success', duration: 1500 })
        return result
      } else {
        error.value = '登录失败，请检查后端连接'
        uni.showToast({ title: '登录失败，请检查后端连接', icon: 'none', duration: 2000 })
        return null
      }
    } catch (e) {
      console.error('[ZiWei] Login failed:', e)
      error.value = '登录失败，请重试'
      uni.showToast({ title: '登录失败，请重试', icon: 'none', duration: 2000 })
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * Custom uni.login wrapper with timeout
   * @returns {Promise<{code: string}>}
   */
  function uniLogin() {
    return new Promise((resolve, reject) => {
      let settled = false
      const timer = setTimeout(() => {
        if (!settled) {
          settled = true
          reject(new Error('wx.login timeout'))
        }
      }, 15000)

      uni.login({
        success: (res) => {
          if (!settled) {
            settled = true
            clearTimeout(timer)
            resolve(res)
          }
        },
        fail: (err) => {
          if (!settled) {
            settled = true
            clearTimeout(timer)
            reject(err)
          }
        },
      })
    })
  }

  return {
    loading,
    error,
    wechatLogin,
  }
}
