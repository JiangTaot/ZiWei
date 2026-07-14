/**
 * User store — auth state + user profile
 * Split from the original monolithic store. Chart-related state moved to chart.js.
 *
 * Uses pinia-plugin-persist-uni for automatic persistence (no more manual setStorageSync).
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import $api from '@/core/api'

const useUserStore = defineStore('user', () => {
  // === Auth State ===
  const token = ref('')
  const openId = ref('')
  const userId = ref('')

  // === User Profile ===
  const nickName = ref('')
  const avatarUrl = ref('')
  const phoneNumber = ref('')

  // === Preferences ===
  const preferences = ref({
    language: 'zh-CN',
    defaultGender: 'male',
    defaultLocation: '',
    theme: 'classic',
  })

  // === Getters ===
  const isLoggedIn = computed(() => !!token.value)
  const displayName = computed(() => nickName.value || '紫微星人')

  // === Actions ===

  /**
   * Login with WeChat code
   * @param {string} code - WeChat login code
   * @param {string} [nickname] - User nickname
   * @param {string} [avatar] - User avatar URL
   */
  async function login(code, nickname, avatar) {
    try {
      const res = await $api.user.login(code, nickname, avatar)
      const data = res.data || res
      if (data && data.token) {
        token.value = data.token
        openId.value = data.openid || ''
        userId.value = data.userId || ''
        nickName.value = data.nickname || ''
        avatarUrl.value = data.avatar || ''
        // Also keep legacy storage keys for backward compatibility during migration
        uni.setStorageSync('auth_token', data.token)
        uni.setStorageSync('user_openid', data.openid || '')
        uni.setStorageSync('user_id', data.userId || '')
        return data
      }
      return null
    } catch (e) {
      console.error('[ZiWei Store] Login failed:', e.message || e)
      return null
    }
  }

  /**
   * Logout — clear all auth state
   */
  function logout() {
    token.value = ''
    openId.value = ''
    userId.value = ''
    nickName.value = ''
    avatarUrl.value = ''
    phoneNumber.value = ''
    // Clean up legacy storage
    uni.removeStorageSync('auth_token')
    uni.removeStorageSync('user_openid')
    uni.removeStorageSync('user_id')
  }

  /**
   * Update user profile (local + sync to backend)
   * @param {Object} profile - Profile data to update
   * @param {boolean} [syncToServer=true]
   */
  async function updateProfile(profile, syncToServer = true) {
    if (profile.nickName !== undefined) nickName.value = profile.nickName
    if (profile.avatarUrl !== undefined) avatarUrl.value = profile.avatarUrl
    if (profile.phoneNumber !== undefined) phoneNumber.value = profile.phoneNumber

    if (syncToServer && isLoggedIn.value) {
      try {
        await $api.user.updateProfile({
          nickname: nickName.value,
          avatar: avatarUrl.value,
        })
      } catch (e) {
        console.error('[ZiWei Store] Sync profile to server failed:', e)
      }
    }
  }

  /**
   * Update user preferences (partial merge)
   * @param {Object} prefs - Preferences to update
   */
  function updatePreferences(prefs) {
    preferences.value = { ...preferences.value, ...prefs }
  }

  return {
    // State
    token,
    openId,
    userId,
    nickName,
    avatarUrl,
    phoneNumber,
    preferences,
    // Getters
    isLoggedIn,
    displayName,
    // Actions
    login,
    logout,
    updateProfile,
    updatePreferences,
  }
}, {
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'user-store',
        storage: {
          getItem: (key) => uni.getStorageSync(key),
          setItem: (key, value) => uni.setStorageSync(key, value),
        },
      },
    ],
  },
})

export default useUserStore
